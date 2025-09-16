package com.example.mdbbill.peripheral

import android.hardware.mdbMaster.MdbMaster
import android.os.Handler
import android.util.Log
import java.util.*

class CoinChanger(
    mdbMaster: MdbMaster?,
    handler: Handler
) : MdbDeviceBase(mdbMaster, handler) {
    
    companion object {
        private const val ADDRESS = 0x08
        
        // Coin changer command
        private const val RESET = 0x08
        private const val SETUP = 0x09
        private const val TUBE_STATUS = 0x0A
        private const val POLL = 0x0B
        private const val COIN_TYPE = 0x0C
        private const val DISPENSE = 0x0D
        private const val EXPANSION = 0x0F

        // EXPANSION Sub-command
        private const val SUBCMD_EXPANSION_IDENTIFICATION = 0x00
        private const val SUBCMD_EXPANSION_FEATURE_ENABLE = 0x01
        private const val SUBCMD_EXPANSION_PAYOUT = 0x02
        private const val SUBCMD_EXPANSION_PAYOUT_STATUS = 0x03
        private const val SUBCMD_EXPANSION_PAYOUT_VALUE_POLL = 0x04
        private const val SUBCMD_EXPANSION_SEND_DIAGNOSTIC_STATUS = 0x05
        private const val SUBCMD_EXPANSION_SEND_CONTROLLED_MANUAL_FILL_REPORT = 0x06
        private const val SUBCMD_EXPANSION_SEND_CONTROLLED_MANUAL_PAYOUT_REPORT = 0x07
        private const val SUBCMD_EXPANSION_FTL_REQ_TO_RCV = 0xFA
        private const val SUBCMD_EXPANSION_FTL_RETRY = 0xFB
        private const val SUBCMD_EXPANSION_FTL_SEND_BLOCK = 0xFC
        private const val SUBCMD_EXPANSION_FTL_OK_TO_SEND = 0xFD
        private const val SUBCMD_EXPANSION_FTL_REQ_TO_SEND = 0xFE
        private const val SUBCMD_EXPANSION_DIAGNOSTICS = 0xFF

        // POLL response flag
        const val ACTIVITY_TYPE_COIN_DISPENSED_MANUALLY = 0x80
        const val ACTIVITY_TYPE_COINS_DEPOSITED = 0x40
        const val ACTIVITY_TYPE_SLUG = 0x20
        const val ACTIVITY_TYPE_STATUS = 0x00
    }

    init {
        maxResponseTime = 2
    }

    /** Coin changer features */
    var coinTypeSupport = 0
    var tubeStatusSupport = false
    var manualFillSupport = false
    var manualPayoutSupport = false

    /**
     * Enum for coin changer command action
     */
    enum class CoinChangerAction {
        ACT_RESET,
        ACT_SETUP,
        ACT_POLL,
        ACT_TUBE_STATUS,
        ACT_COIN_TYPE,
        ACT_DISPENSE,
        ACT_EXPANSION_IDENTIFICATION,
        ACT_EXPANSION_FEATURE_ENABLE
    }

    private var actuator = CoinChangerAction.ACT_RESET

    /**
     * Timer for Non-Response time
     */
    private var coinChangerTimer: Timer? = null
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
        if (coinChangerTimer == null) {
            maxNonRespTimerTask = object : TimerTask() {
                override fun run() {
                    Log.d(Constant.TAG, "Coin changer does not respond within its maximum Non-Response time.")
                    isOnline = false
                    coinChangerTimer = null
                }
            }

            coinChangerTimer = Timer()
            coinChangerTimer?.schedule(maxNonRespTimerTask, maxResponseTime * 1000L)
        }
    }

    private fun maxNonRespTimerCancel() {
        coinChangerTimer?.cancel()
        coinChangerTimer?.purge()
        coinChangerTimer = null
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
        actuator = CoinChangerAction.ACT_SETUP
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
            actuator = CoinChangerAction.ACT_EXPANSION_IDENTIFICATION
        } else if (ret > 1) {
            sendMdbAnswer(Constant.ACK)
            // Parse setup response
            featureLevel = response[1].toInt()
            coinTypeSupport = response[2].toInt()
            tubeStatusSupport = (response[3].toInt() and 0x01) != 0
            manualFillSupport = (response[3].toInt() and 0x02) != 0
            manualPayoutSupport = (response[3].toInt() and 0x04) != 0
            actuator = CoinChangerAction.ACT_EXPANSION_IDENTIFICATION
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
            ACTIVITY_TYPE_COINS_DEPOSITED -> {
                Log.d(Constant.TAG, "Coins deposited")
                handler.sendEmptyMessage(Event.EV_COIN_CHANGER_ACTIVITY_TYPE_REPORT)
            }
            ACTIVITY_TYPE_COIN_DISPENSED_MANUALLY -> {
                Log.d(Constant.TAG, "Coin dispensed manually")
                handler.sendEmptyMessage(Event.EV_COIN_CHANGER_ACTIVITY_TYPE_REPORT)
            }
            ACTIVITY_TYPE_SLUG -> {
                Log.d(Constant.TAG, "Slug detected")
                handler.sendEmptyMessage(Event.EV_COIN_CHANGER_ACTIVITY_TYPE_REPORT)
            }
            ACTIVITY_TYPE_STATUS -> {
                // Handle status response
                if (ret > 1) {
                    val status = response[1].toInt()
                    if ((status and 0x01) != 0) {
                        Log.d(Constant.TAG, "Coin changer just reset")
                    }
                    if ((status and 0x02) != 0) {
                        Log.d(Constant.TAG, "Coin changer enter service mode")
                    }
                    if ((status and 0x04) != 0) {
                        Log.d(Constant.TAG, "Coin changer enter sale mode")
                    }
                }
            }
        }
    }

    private fun expansionIdentification() {
        var checksum = 0
        val response = ByteArray(36)
        
        cmdBuffer[0] = EXPANSION.toByte()
        cmdBuffer[1] = SUBCMD_EXPANSION_IDENTIFICATION.toByte()
        
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
            
            actuator = CoinChangerAction.ACT_EXPANSION_FEATURE_ENABLE
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
        actuator = CoinChangerAction.ACT_POLL
        // Notify a coin changer detected
        handler.sendEmptyMessage(Event.EV_COIN_CHANGER_DETECTED)
    }

    override fun process() {
        // In simulation mode, only process specific actions, not continuous polling
        if (mdbMaster == null) {
            when (actuator) {
                CoinChangerAction.ACT_RESET -> {
                    // Simulate successful reset
                    actuator = CoinChangerAction.ACT_POLL
                    isOnline = true
                    initializingSequenceFinish = true
                    Log.d(Constant.TAG, "Simulation mode: CoinChanger reset completed")
                }
                // Skip continuous polling in simulation mode
                CoinChangerAction.ACT_POLL -> {
                    // Do nothing in simulation mode to prevent infinite loop
                }
                else -> {
                    // For other actions, just log and set to POLL
                    Log.d(Constant.TAG, "Simulation mode: Skipping action $actuator")
                    actuator = CoinChangerAction.ACT_POLL
                }
            }
            return
        }
        
        // Real hardware mode - process normally
        when (actuator) {
            CoinChangerAction.ACT_RESET -> reset()
            CoinChangerAction.ACT_SETUP -> setup()
            CoinChangerAction.ACT_POLL -> poll()
            CoinChangerAction.ACT_EXPANSION_IDENTIFICATION -> expansionIdentification()
            CoinChangerAction.ACT_EXPANSION_FEATURE_ENABLE -> expansionFeatureEnable()
            else -> {
                // Handle other actions if needed
            }
        }
    }

    fun setActuator(actuator: CoinChangerAction) {
        this.actuator = actuator
    }

    fun clean() {
        maxNonRespTimerCancel()
        initializingSequenceFinish = false
        actuator = CoinChangerAction.ACT_RESET
    }
}
