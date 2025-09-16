package com.example.mdbbill.peripheral

import android.hardware.mdbMaster.MdbMaster
import android.os.Handler
import android.util.Log
import java.util.*

class BillValidator(
    mdbMaster: MdbMaster?,
    handler: Handler
) : MdbDeviceBase(mdbMaster, handler) {
    
    companion object {
        private const val ADDRESS = 0x30
        
        // Bill validator command
        private const val RESET = 0x30
        private const val SETUP = 0x31
        private const val SECURITY = 0x32
        private const val POLL = 0x33
        private const val BILL_TYPE = 0x34
        private const val ESCROW = 0x35
        private const val STACKER = 0x36
        private const val EXPANSION = 0x37

        // EXPANSION Sub-command
        private const val SUBCMD_EXPANSION_IDENTIFICATION_WITHOUT_OPTION_BIT = 0x00
        private const val SUBCMD_EXPANSION_FEATURE_ENABLE = 0x01
        private const val SUBCMD_EXPANSION_IDENTIFICATION_WITH_OPTION_BITS = 0x02
        private const val SUBCMD_EXPANSION_RECYCLER_SETUP = 0x03
        private const val SUBCMD_EXPANSION_RECYCLER_ENABLE = 0x04
        private const val SUBCMD_EXPANSION_BILL_DISPENSE_STATUS = 0x05
        private const val SUBCMD_EXPANSION_DISPENSE_BILL = 0x06
        private const val SUBCMD_EXPANSION_DISPENSE_VALUE = 0x07
        private const val SUBCMD_EXPANSION_PAYOUT_STATUS = 0x08
        private const val SUBCMD_EXPANSION_PAYOUT_VALUE_POLL = 0x09
        private const val SUBCMD_EXPANSION_PAYOUT_CANCEL = 0x0A
        private const val SUBCMD_EXPANSION_FTL_REQ_TO_RCV = 0xFA
        private const val SUBCMD_EXPANSION_FTL_RETRY = 0xFB
        private const val SUBCMD_EXPANSION_FTL_SEND_BLOCK = 0xFC
        private const val SUBCMD_EXPANSION_FTL_OK_TO_SEND = 0xFD
        private const val SUBCMD_EXPANSION_FTL_REQ_TO_SEND = 0xFE
        private const val SUBCMD_EXPANSION_DIAGNOSTICS = 0xFF

        // POLL response flag
        const val ACTIVITY_TYPE_BILL_STACKED = 0x80
        const val ACTIVITY_TYPE_BILL_REJECTED = 0x40
        const val ACTIVITY_TYPE_BILL_ESCROW_POSITION = 0x20
        const val ACTIVITY_TYPE_BILL_RETURNED = 0x10
        const val ACTIVITY_TYPE_BILL_ACCEPTED = 0x08
        const val ACTIVITY_TYPE_BILL_INSERTED = 0x04
        const val ACTIVITY_TYPE_BILL_ACCEPTED_AND_STACKED = 0x02
        const val ACTIVITY_TYPE_BILL_ACCEPTED_AND_RETURNED = 0x01
        const val ACTIVITY_TYPE_STATUS = 0x00
    }

    init {
        maxResponseTime = 2
    }

    /** Bill validator features */
    var billTypeSupport = 0
    var securityLevel = 0
    var escrowSupport = false
    var fullEscrowSupport = false
    var recyclerSupport = false

    /**
     * Enum for bill validator command action
     */
    enum class BillValidatorAction {
        ACT_RESET,
        ACT_SETUP,
        ACT_POLL,
        ACT_BILL_TYPE,
        ACT_ESCROW,
        ACT_STACKER,
        ACT_EXPANSION_IDENTIFICATION,
        ACT_EXPANSION_FEATURE_ENABLE
    }

    private var actuator = BillValidatorAction.ACT_RESET

    /**
     * Timer for Non-Response time
     */
    private var billValidatorTimer: Timer? = null
    private var maxNonRespTimerTask: TimerTask? = null
    
    /**
     * Helper function to safely send MDB commands
     * Returns -1 if in simulation mode, otherwise returns the actual result
     */
    private fun sendMdbCommand(cmdBuffer: ByteArray, length: Int, response: ByteArray): Int {
        return if (mdbMaster == null) {
            // Simulation mode - return success
            Log.d(Constant.TAG, "Simulation mode: Simulating MDB command success")
            response[0] = Constant.ACK.toByte()
            1
        } else {
            // Real hardware mode
            mdbMaster.sendCommand(cmdBuffer, length, response)
        }
    }
    
    /**
     * Helper function to safely send MDB answers
     * Does nothing in simulation mode, otherwise sends the answer
     */
    private fun sendMdbAnswer(answer: Int) {
        if (mdbMaster == null) {
            // Simulation mode - do nothing
            Log.d(Constant.TAG, "Simulation mode: Simulating MDB answer $answer")
        } else {
            // Real hardware mode
            mdbMaster.sendAnswer(answer)
        }
    }

    private fun maxNonRespTimerStart() {
        if (billValidatorTimer == null) {
            maxNonRespTimerTask = object : TimerTask() {
                override fun run() {
                    Log.d(Constant.TAG, "Bill validator does not respond within its maximum Non-Response time.")
                    isOnline = false
                    billValidatorTimer = null
                }
            }

            billValidatorTimer = Timer()
            billValidatorTimer?.schedule(maxNonRespTimerTask, maxResponseTime * 1000L)
        }
    }

    private fun maxNonRespTimerCancel() {
        billValidatorTimer?.cancel()
        billValidatorTimer?.purge()
        billValidatorTimer = null
    }

    private fun isTransmissionSuccess(result: Int): Boolean {
        // Send command error, the command should be re-sent
        if (result == MdbMaster.ERR_FAIL || result == MdbMaster.ERR_WRITE) {
            return false
        }

        // Send success, but received error, the command should be re-sent
        if (result == MdbMaster.ERR_NONRESP || result == MdbMaster.ERR_CHECKSUM || result == MdbMaster.ERR_READ) {
            // Non response within timeout, start app Non-Response timer
            if (result == MdbMaster.ERR_NONRESP) {
                maxNonRespTimerStart()
            }
            return false
        }

        // Transmission success, cancel Non Response timer if possible
        maxNonRespTimerCancel()
        return true
    }

    private fun reset() {
        val response = ByteArray(36)

        cmdBuffer[0] = RESET.toByte()
        cmdBuffer[1] = cmdBuffer[0]
        val ret = sendMdbCommand(cmdBuffer, 2, response)
        
        // If response size is not 1 that means its not a correct response
        // or received NAK - in this case, the command would be re-sent
        if (ret != 1 || response[0] == Constant.NAK.toByte()) {
            return
        }

        // ACK received
        actuator = BillValidatorAction.ACT_SETUP
    }

    private fun setup() {
        var checksum = 0
        val response = ByteArray(36)
        
        cmdBuffer[0] = SETUP.toByte()
        cmdBuffer[1] = 0x00 // Feature level
        cmdBuffer[2] = 0x00 // Feature level
        cmdBuffer[3] = 0x00 // Feature level
        cmdBuffer[4] = 0x00 // Feature level
        cmdBuffer[5] = 0x00 // Feature level
        cmdBuffer[6] = 0x00 // Feature level
        cmdBuffer[7] = 0x00 // Feature level
        cmdBuffer[8] = 0x00 // Feature level
        
        for (i in 0..8) {
            checksum += cmdBuffer[i].toInt() and 0xFF
        }
        cmdBuffer[9] = (checksum and 0xFF).toByte()
        
        val ret = sendMdbCommand(cmdBuffer, 10, response)
        if (!isTransmissionSuccess(ret)) {
            return
        }

        if (ret == 1 && response[0] == Constant.ACK.toByte()) {
            actuator = BillValidatorAction.ACT_EXPANSION_IDENTIFICATION
        } else if (ret > 1) {
            sendMdbAnswer(Constant.ACK)
            // Parse setup response
            featureLevel = response[1].toInt()
            billTypeSupport = response[2].toInt()
            securityLevel = response[3].toInt()
            escrowSupport = (response[4].toInt() and 0x01) != 0
            fullEscrowSupport = (response[4].toInt() and 0x02) != 0
            recyclerSupport = (response[4].toInt() and 0x04) != 0
            actuator = BillValidatorAction.ACT_EXPANSION_IDENTIFICATION
        }
    }

    private fun poll() {
        val response = ByteArray(36)
        cmdBuffer[0] = POLL.toByte()
        cmdBuffer[1] = POLL.toByte()
        val ret = sendMdbCommand(cmdBuffer, 2, response)
        
        // Transmission failure, return directly, the command would be re-sent
        if (!isTransmissionSuccess(ret)) {
            return
        }

        // If ret == 1 that means received ACK or NAK
        if (ret == 1) {
            return
        }

        // Has response data:
        // 1. reply ACK to peripheral
        // 2. cancel Max non response timer
        // 3. handle response data
        sendMdbAnswer(Constant.ACK)
        
        when (response[0].toInt()) {
            ACTIVITY_TYPE_BILL_INSERTED -> {
                Log.d(Constant.TAG, "Bill inserted")
                handler.sendEmptyMessage(Event.EV_BILL_VALIDATOR_INSERT_BILL)
            }
            ACTIVITY_TYPE_BILL_ACCEPTED -> {
                Log.d(Constant.TAG, "Bill accepted")
                handler.sendEmptyMessage(Event.EV_BILL_VALIDATOR_ENTER_SALE_MODE)
            }
            ACTIVITY_TYPE_BILL_STACKED -> {
                Log.d(Constant.TAG, "Bill stacked")
                handler.sendEmptyMessage(Event.EV_BILL_VALIDATOR_STACKERED_IN_ESCROW)
            }
            ACTIVITY_TYPE_BILL_REJECTED -> {
                Log.d(Constant.TAG, "Bill rejected")
                handler.sendEmptyMessage(Event.EV_BILL_VALIDATOR_RETURNED_IN_ESCROW)
            }
            ACTIVITY_TYPE_STATUS -> {
                // Handle status response
                if (ret > 1) {
                    val status = response[1].toInt()
                    if ((status and 0x01) != 0) {
                        Log.d(Constant.TAG, "Bill validator just reset")
                        handler.sendEmptyMessage(Event.EV_BILL_VALIDATOR_JUST_RESET)
                    }
                    if ((status and 0x02) != 0) {
                        Log.d(Constant.TAG, "Bill validator enter service mode")
                        handler.sendEmptyMessage(Event.EV_BILL_VALIDATOR_ENTER_SERVICE_MODE)
                    }
                    if ((status and 0x04) != 0) {
                        Log.d(Constant.TAG, "Bill validator enter sale mode")
                        handler.sendEmptyMessage(Event.EV_BILL_VALIDATOR_ENTER_SALE_MODE)
                    }
                }
            }
        }
    }

    private fun expansionIdentification() {
        var checksum = 0
        val response = ByteArray(36)
        
        cmdBuffer[0] = EXPANSION.toByte()
        cmdBuffer[1] = SUBCMD_EXPANSION_IDENTIFICATION_WITHOUT_OPTION_BIT.toByte()
        
        for (i in 0..1) {
            checksum += cmdBuffer[i].toInt() and 0xFF
        }
        cmdBuffer[2] = (checksum and 0xFF).toByte()
        
        val ret = sendMdbCommand(cmdBuffer, 3, response)
        if (!isTransmissionSuccess(ret)) {
            return
        }

        if (ret > 1) {
            sendMdbAnswer(Constant.ACK)
            // Parse identification response
            System.arraycopy(response, 1, manufacturerCode, 0, 3)
            System.arraycopy(response, 4, serialNumber, 0, 12)
            System.arraycopy(response, 16, modelNumber, 0, 12)
            System.arraycopy(response, 28, softwareVersion, 0, 2)
            
            actuator = BillValidatorAction.ACT_EXPANSION_FEATURE_ENABLE
        }
    }

    private fun expansionFeatureEnable() {
        var checksum = 0
        val response = ByteArray(36)
        
        cmdBuffer[0] = EXPANSION.toByte()
        cmdBuffer[1] = SUBCMD_EXPANSION_FEATURE_ENABLE.toByte()
        cmdBuffer[2] = 0x00
        cmdBuffer[3] = 0x00
        cmdBuffer[4] = 0x00
        cmdBuffer[5] = 0x00
        
        for (i in 0..5) {
            checksum += cmdBuffer[i].toInt() and 0xFF
        }
        cmdBuffer[6] = (checksum and 0xFF).toByte()
        
        val ret = sendMdbCommand(cmdBuffer, 7, response)
        if (ret != 1 || response[0] != Constant.ACK.toByte()) {
            return
        }

        initializingSequenceFinish = true
        actuator = BillValidatorAction.ACT_POLL
        // Notify a bill validator detected
        handler.sendEmptyMessage(Event.EV_BILL_VALIDATOR_DETECTED)
    }

    override fun process() {
        // In simulation mode, only process specific actions, not continuous polling
        if (mdbMaster == null) {
            when (actuator) {
                BillValidatorAction.ACT_RESET -> {
                    // Simulate successful reset
                    actuator = BillValidatorAction.ACT_POLL
                    isOnline = true
                    initializingSequenceFinish = true
                    Log.d(Constant.TAG, "Simulation mode: BillValidator reset completed")
                }
                // Skip continuous polling in simulation mode
                BillValidatorAction.ACT_POLL -> {
                    // Do nothing in simulation mode to prevent infinite loop
                }
                else -> {
                    // For other actions, just log and set to POLL
                    Log.d(Constant.TAG, "Simulation mode: Skipping action $actuator")
                    actuator = BillValidatorAction.ACT_POLL
                }
            }
            return
        }
        
        // Real hardware mode - process normally
        when (actuator) {
            BillValidatorAction.ACT_RESET -> reset()
            BillValidatorAction.ACT_SETUP -> setup()
            BillValidatorAction.ACT_POLL -> poll()
            BillValidatorAction.ACT_EXPANSION_IDENTIFICATION -> expansionIdentification()
            BillValidatorAction.ACT_EXPANSION_FEATURE_ENABLE -> expansionFeatureEnable()
            else -> {
                // Handle other actions if needed
            }
        }
    }

    fun setActuator(actuator: BillValidatorAction) {
        this.actuator = actuator
    }

    fun clean() {
        maxNonRespTimerCancel()
        initializingSequenceFinish = false
        actuator = BillValidatorAction.ACT_RESET
    }
}
