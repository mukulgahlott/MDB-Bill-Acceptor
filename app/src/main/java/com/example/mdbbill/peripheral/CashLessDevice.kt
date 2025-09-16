package com.example.mdbbill.peripheral

import android.hardware.mdbMaster.MdbMaster
import android.os.Handler
import android.util.Log
import com.example.mdbbill.mdb.VendingMachineController
import java.util.*

class CashLessDevice(
    mdbMaster: MdbMaster?,
    handler: Handler
) : MdbDeviceBase(mdbMaster, handler) {
    
    companion object {
        private const val ADDRESS = 0x10
        
        // Cashless device features
        private const val CASHLESS_RESET = 0x10
        private const val CASHLESS_SETUP = 0x11
        private const val CASHLESS_POLL = 0x12
        private const val CASHLESS_VEND = 0x13
        private const val CASHLESS_READER = 0x14
        private const val CASHLESS_REVALUE = 0x15
        private const val CASHLESS_EXPANSION = 0x17

        // SETUP sub command
        private const val SUBCMD_SETUP_CONFIG_DATA = 0x00
        private const val SUBCMD_SETUP_MAXMIN_PRICES = 0x01

        // POLL Response id
        private const val RESP_ID_JUST_RESET = 0x00
        private const val RESP_ID_READER_CONFIG_DATA = 0x01
        private const val RESP_ID_DISPLAY_REQUEST = 0x02
        private const val RESP_ID_BEGIN_SESSION = 0x03
        private const val RESP_ID_SESSION_CANCEL_REQUEST = 0x04
        private const val RESP_ID_VEND_APPROVED = 0x05
        private const val RESP_ID_VEND_DENIED = 0x06
        private const val RESP_ID_END_SESSION = 0x07
        private const val RESP_ID_CANCELED = 0x08
        private const val RESP_ID_PERIPHERAL_ID = 0x09
        private const val RESP_ID_MALFUNCTION = 0x0A
        private const val RESP_ID_CMD_OUT_OF_SEQUENCE = 0x0B
        private const val RESP_ID_REVALUE_APPROVED = 0x0D
        private const val RESP_ID_REVALUE_DENIED = 0x0E
        private const val RESP_ID_REVALUE_REVALUE_LIMIT_AMOUNT = 0x0F
        private const val RESP_ID_TIME_DATE_REQUEST = 0x11
        private const val RESP_ID_DATA_ENTRY_REQUEST = 0x12
        private const val RESP_ID_DATA_ENTRY_CANCEL = 0x13
        private const val RESP_ID_DIAGNOSTICS = 0xFF

        // VEND sub command
        private const val SUBCMD_VEND_REQUEST = 0x00
        private const val SUBCMD_VEND_CANCEL = 0x01
        private const val SUBCMD_VEND_SUCCESS = 0x02
        private const val SUBCMD_VEND_FAILURE = 0x03
        private const val SUBCMD_SESSION_COMPLETE = 0x04
        private const val SUBCMD_CASH_SALE = 0x05
        private const val SUBCMD_NEGATIVE_VEND_REQUESET = 0x06

        // READER sub command
        private const val SUBCMD_READER_DISABLE = 0x00
        private const val SUBCMD_READER_ENABLE = 0x01
        private const val SUBCMD_READER_CANCEL = 0x02
        private const val SUBCMD_DATA_ENTRY_RESPONSE = 0x03

        // REVALUE sub command
        private const val SUBCMD_REVALUE_REQUEST = 0x00
        private const val SUBCMD_REVALUE_LIMIT_REQUEST = 0x01

        // EXPANSION sub command
        private const val SUBCMD_EXPANSION_REQUEST_ID = 0x00
        private const val SUBCMD_EXPANSION_READER_USER_FILE = 0x01 // Obsolete Command
        private const val SUBCMD_EXPANSION_WRITE_USER_FILE = 0x02 // Obsolete Command
        private const val SUBCMD_EXPANSION_WRITE_TIMEDATA = 0x03
        private const val SUBCMD_EXPANSION_OPTION_FEATURE_ENABLE = 0x04
    }

    init {
        maxResponseTime = 5
    }

    /**cashless device features*/
    var miscOptions = 0x00
    val cashlessOptionFeatureBit = ByteArray(4)

    /**
     * some variables
     */
    private var itemPrice = 1
    private var itemNumber = 1
    private var isDispensedFailure = false

    /**
     * Enum for cashless command action
     */
    enum class CashlessAction {
        ACT_RESET,
        ACT_SETUP_CONFIG_DATA,
        ACT_SETUP_MAXMIN_PRICES,
        ACT_POLL,
        ACT_VEND_REQUEST,
        ACT_VEND_CANCEL,
        ACT_VEND_SUCCESS,
        ACT_VEND_FAILURE,
        ACT_SESSION_COMPLETE,
        ACT_CASH_SALE,
        ACT_NEGATIVE_VEND_REQUESET,
        ACT_READER_DISABLE,
        ACT_READER_ENABLE,
        ACT_READER_CANCEL,
        ACT_DATA_ENTRY_RESPONSE,
        ACT_REVALUE_REQUEST,
        ACT_REVALUE_LIMIT_REQUEST,
        ACT_EXPANSION_REQUEST_ID,
        ACT_EXPANSION_OPTION_FEATURE_ENABLE
    }

    private var actuator = CashlessAction.ACT_RESET

    /**
     * Timer for Non-Response time
     */
    private var cashLessTimer: Timer? = null
    
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
    private var maxNonRespTimerTask: TimerTask? = null

    private fun maxNonRespTimerStart() {
        if (cashLessTimer == null) {
            maxNonRespTimerTask = object : TimerTask() {
                override fun run() {
                    Log.d(Constant.TAG, "cashless does not respond within its maximum Non-Response time.")
                    isOnline = false
                    cashLessTimer = null
                }
            }

            cashLessTimer = Timer()
            cashLessTimer?.schedule(maxNonRespTimerTask, maxResponseTime * 1000L)
        }
    }

    private fun maxNonRespTimerCancel() {
        cashLessTimer?.cancel()
        cashLessTimer?.purge()
        cashLessTimer = null
    }

    private fun isTransmissionSuccess(result: Int): Boolean {
        // Send command error, the command should be re-sent
        if (result == MdbMaster.ERR_FAIL || result == MdbMaster.ERR_WRITE) {
            return false
        }

        // Send success, but received error, the command should be re-sent
        if (result == MdbMaster.ERR_NONRESP || result == MdbMaster.ERR_CHECKSUM || result == MdbMaster.ERR_READ) {
            // Non response within 5ms, start app Non-Response timer
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

        cmdBuffer[0] = CASHLESS_RESET.toByte()
        cmdBuffer[1] = cmdBuffer[0]
        val ret = sendMdbCommand(cmdBuffer, 2, response)
        
        // If response size is not 1 that means its not a correct response
        // or received NAK - in this case, the command would be re-sent
        if (ret != 1 || response[0] == Constant.NAK.toByte()) {
            return
        }

        // ACK received
        actuator = CashlessAction.ACT_POLL
    }

    private fun poll() {
        val response = ByteArray(36)
        cmdBuffer[0] = CASHLESS_POLL.toByte()
        cmdBuffer[1] = CASHLESS_POLL.toByte()
        val ret = sendMdbCommand(cmdBuffer, 2, response)
        
        // Transmission failure, return directly, the command would be re-sent
        if (!isTransmissionSuccess(ret)) {
            return
        }

        // If ret == 1 that means received ACK or NAK
        if (ret == 1) {
            // Has product dispensed failure and replied ACK (refund complete), SESSION COMPLETE should be sent
            if (isDispensedFailure && response[0] == Constant.ACK.toByte()) {
                actuator = CashlessAction.ACT_SESSION_COMPLETE
                isDispensedFailure = false
            }
            return
        }

        // Has response data:
        // 1. reply ACK to peripheral
        // 2. cancel Max non response timer
        // 3. handle response data
        sendMdbAnswer(Constant.ACK)
        
        when (response[0].toInt()) {
            RESP_ID_JUST_RESET -> {
                if (!initializingSequenceFinish) {
                    actuator = CashlessAction.ACT_SETUP_CONFIG_DATA
                } else {
                    // Anytime a POLL command results in a "JUST RESET" response means peripheral self resets,
                    // in this case, initialization sequence should be executed
                    isOnline = false
                    handler.sendEmptyMessage(Event.EV_CASHLESS_NON_RESPONSE)
                    actuator = CashlessAction.ACT_RESET
                }
            }
            RESP_ID_READER_CONFIG_DATA -> {
                featureLevel = response[1].toInt()
                currencyCode = (response[2].toInt() shl 8) or response[3].toInt()
                scaleFactor = response[4].toInt()
                decimalPlaces = response[5].toInt()
                maxResponseTime = response[6].toInt()
                miscOptions = response[7].toInt()
                if (!initializingSequenceFinish) {
                    actuator = CashlessAction.ACT_SETUP_MAXMIN_PRICES
                } else {
                    actuator = CashlessAction.ACT_READER_ENABLE
                }
            }
            RESP_ID_DISPLAY_REQUEST -> {
                // Handle display request
            }
            RESP_ID_BEGIN_SESSION -> {
                Log.d(Constant.TAG, "Begin Session")
                // Notify activity, cashless device begin session
                handler.sendEmptyMessage(Event.EV_CASHLESS_BEGIN_SESSION)
            }
            RESP_ID_SESSION_CANCEL_REQUEST -> {
                // Session cancel, SESSION COMPLETE would be sent
                actuator = CashlessAction.ACT_SESSION_COMPLETE
                // Notify activity session cancel
                handler.sendEmptyMessage(Event.EV_CASHLESS_SESSION_CANCEL)
            }
            RESP_ID_VEND_APPROVED -> {
                // Notify activity vend approved, and then application should inform Vend Success or not
                actuator = CashlessAction.ACT_VEND_SUCCESS
                handler.sendEmptyMessage(Event.EV_CASHLESS_VEND_APPROVED)
            }
            RESP_ID_VEND_DENIED -> {
                // Vend denied, SESSION COMPLETE would be sent
                actuator = CashlessAction.ACT_SESSION_COMPLETE
                // Notify activity vend denied
                handler.sendEmptyMessage(Event.EV_CASHLESS_VEND_DENIED)
            }
            RESP_ID_END_SESSION -> {
                handler.sendEmptyMessage(Event.EV_CASHLESS_END_SESSION)
                actuator = CashlessAction.ACT_READER_ENABLE
            }
            RESP_ID_MALFUNCTION -> {
                val errorCode = response[1]
                // If error code is 1100yyyy means refund fail, SESSION COMPLETE should be sent
                if ((errorCode.toInt() shr 4) == 0x0c && isDispensedFailure) {
                    actuator = CashlessAction.ACT_SESSION_COMPLETE
                    isDispensedFailure = false
                }
            }
            RESP_ID_CMD_OUT_OF_SEQUENCE -> {
                // Cmd out of sequence, RESET should be sent
                handler.sendEmptyMessage(Event.EV_CASHLESS_CMD_OUT_OF_SEQUENCE)
                actuator = CashlessAction.ACT_RESET
            }
        }
    }

    private fun setupConfigData() {
        var checksum = 0
        val response = ByteArray(36)
        
        cmdBuffer[0] = CASHLESS_SETUP.toByte()
        cmdBuffer[1] = SUBCMD_SETUP_CONFIG_DATA.toByte()
        cmdBuffer[2] = VendingMachineController.getInstance().vmcFeatureLevel.toByte()
        cmdBuffer[3] = 0x00 // Columns on Display
        cmdBuffer[4] = 0x00 // Rows on Display
        cmdBuffer[5] = 0x01 // Display Information; 0x01 - Full ASCII
        
        for (i in 0..5) {
            checksum += cmdBuffer[i].toInt() and 0xFF
        }
        cmdBuffer[6] = (checksum and 0xFF).toByte()
        
        val ret = sendMdbCommand(cmdBuffer, 7, response)

        if (ret == 1 && response[0] == Constant.ACK.toByte()) {
            actuator = CashlessAction.ACT_SETUP_MAXMIN_PRICES
        } else if (ret == 9 && response[0] == RESP_ID_READER_CONFIG_DATA.toByte()) {
            sendMdbAnswer(Constant.ACK)
            featureLevel = response[1].toInt()
            currencyCode = (response[2].toInt() shl 8) or response[3].toInt()
            scaleFactor = response[4].toInt()
            decimalPlaces = response[5].toInt()
            maxResponseTime = response[6].toInt()
            miscOptions = response[7].toInt()
            actuator = CashlessAction.ACT_SETUP_MAXMIN_PRICES
        }
    }

    private fun setupMaxMinPrices() {
        var checksum = 0
        val response = ByteArray(36)
        
        cmdBuffer[0] = CASHLESS_SETUP.toByte()
        cmdBuffer[1] = SUBCMD_SETUP_MAXMIN_PRICES.toByte()
        cmdBuffer[2] = 0xFF.toByte()  // Maximum Price – scaled
        cmdBuffer[3] = 0xFF.toByte()
        cmdBuffer[4] = 0x00        // Minimum Price – scaled
        cmdBuffer[5] = 0x00
        
        for (i in 0..5) {
            checksum += cmdBuffer[i].toInt() and 0xFF
        }
        cmdBuffer[6] = (checksum and 0xFF).toByte()
        
        val ret = sendMdbCommand(cmdBuffer, 7, response)
        if (ret == 1 && response[0] == Constant.ACK.toByte()) {
            actuator = CashlessAction.ACT_EXPANSION_REQUEST_ID
        }
    }

    private fun expansionRequestId() {
        var checksum = 0
        var index = 0
        val response = ByteArray(36)
        
        cmdBuffer[index++] = CASHLESS_EXPANSION.toByte()
        cmdBuffer[index++] = SUBCMD_EXPANSION_REQUEST_ID.toByte()
        cmdBuffer[index++] = VendingMachineController.getInstance().manufacturerCode[0]
        cmdBuffer[index++] = VendingMachineController.getInstance().manufacturerCode[1]
        cmdBuffer[index++] = VendingMachineController.getInstance().manufacturerCode[2]
        
        System.arraycopy(VendingMachineController.getInstance().serialNumber, 0, cmdBuffer, index, 12)
        index += 12
        System.arraycopy(VendingMachineController.getInstance().modelNumber, 0, cmdBuffer, index, 12)
        index += 12
        cmdBuffer[index++] = VendingMachineController.getInstance().softwareVersion[0]
        cmdBuffer[index++] = VendingMachineController.getInstance().softwareVersion[1]
        
        for (i in 0..30) {
            checksum += cmdBuffer[i].toInt() and 0xFF
        }
        cmdBuffer[index] = (checksum and 0xFF).toByte()

        val ret = sendMdbCommand(cmdBuffer, 32, response)
        if (!isTransmissionSuccess(ret)) {
            return
        }
        sendMdbAnswer(Constant.ACK)

        System.arraycopy(response, 1, manufacturerCode, 0, 3)
        System.arraycopy(response, 4, serialNumber, 0, 12)
        System.arraycopy(response, 16, modelNumber, 0, 12)
        System.arraycopy(response, 28, softwareVersion, 0, 2)
        
        if (VendingMachineController.getInstance().vmcFeatureLevel >= 3 && ret == 34) {
            System.arraycopy(response, 30, cashlessOptionFeatureBit, 0, 4)
        }

        if (featureLevel == 3) {
            actuator = CashlessAction.ACT_EXPANSION_OPTION_FEATURE_ENABLE
        } else {
            initializingSequenceFinish = true
            actuator = CashlessAction.ACT_READER_ENABLE
            // Notify a cashless detected
            handler.sendEmptyMessage(Event.EV_CASHLESS_DETECTED)
        }
    }

    private fun expansionOptionEnable() {
        var checksum = 0
        val response = ByteArray(36)
        
        cmdBuffer[0] = CASHLESS_EXPANSION.toByte()
        cmdBuffer[1] = SUBCMD_EXPANSION_OPTION_FEATURE_ENABLE.toByte()
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
        actuator = CashlessAction.ACT_READER_ENABLE
        // Notify a cashless detected
        handler.sendEmptyMessage(Event.EV_CASHLESS_DETECTED)
    }

    private fun readerEnable() {
        var checksum = 0
        val response = ByteArray(36)
        
        cmdBuffer[0] = CASHLESS_READER.toByte()
        cmdBuffer[1] = SUBCMD_READER_ENABLE.toByte()
        checksum = (cmdBuffer[0].toInt() and 0xFF) + (cmdBuffer[1].toInt() and 0xFF)
        cmdBuffer[2] = (checksum and 0xFF).toByte()
        
        val ret = sendMdbCommand(cmdBuffer, 3, response)
        if (ret == 1 && response[0] == Constant.ACK.toByte()) {
            actuator = CashlessAction.ACT_POLL
        }
    }

    private fun vendRequest() {
        var checksum = 0
        val response = ByteArray(36)
        
        cmdBuffer[0] = CASHLESS_VEND.toByte()
        cmdBuffer[1] = SUBCMD_VEND_REQUEST.toByte()
        cmdBuffer[2] = ((itemPrice shr 8) and 0xFF).toByte()
        cmdBuffer[3] = (itemPrice and 0xFF).toByte()
        cmdBuffer[4] = ((itemNumber shr 8) and 0xFF).toByte()
        cmdBuffer[5] = (itemNumber and 0xFF).toByte()
        
        for (i in 0..5) {
            checksum += cmdBuffer[i].toInt() and 0xFF
        }
        cmdBuffer[6] = (checksum and 0xFF).toByte()

        val ret = sendMdbCommand(cmdBuffer, 7, response)
        if (!isTransmissionSuccess(ret)) {
            return
        }

        // If response size is not 1 that means its not a correct response for VEND REQUEST
        // or received NAK - in this case, the command would be re-sent
        if (ret != 1 || response[0] == Constant.NAK.toByte()) {
            return
        }

        // Received ACK, POLL would be sent
        actuator = CashlessAction.ACT_POLL
    }

    private fun vendCancel() {
        var checksum = 0
        val response = ByteArray(36)
        
        cmdBuffer[0] = CASHLESS_VEND.toByte()
        cmdBuffer[1] = SUBCMD_VEND_CANCEL.toByte()
        checksum = (cmdBuffer[0].toInt() and 0xFF) + (cmdBuffer[1].toInt() and 0xFF)
        cmdBuffer[2] = (checksum and 0xFF).toByte()
        
        val ret = sendMdbCommand(cmdBuffer, 3, response)
        if (!isTransmissionSuccess(ret)) {
            return
        }

        // If response size is not 1 that means its not a correct response for VEND CANCEL
        // or received NAK - in this case, the command would be re-sent
        if (ret != 1 || response[0] == Constant.NAK.toByte()) {
            return
        }

        // Received ACK, POLL would be sent
        actuator = CashlessAction.ACT_POLL
    }

    fun vendSuccess() {
        Log.d(Constant.TAG, "vendSuccess() called - starting background thread for MDB communication")
        
        // Check if we're in simulation mode (no hardware)
        if (mdbMaster == null) {
            Log.d(Constant.TAG, "Simulation mode: Simulating VEND SUCCESS without hardware")
            
            // Simulate the MDB communication in a background thread
            Thread {
                try {
                    // Simulate processing time
                    Thread.sleep(100)
                    
                    if (itemNumber == 0) {
                        Log.d(Constant.TAG, "SIMULATION: Amount-based vending successful: VMC received ${itemPrice} cents ($${itemPrice / 100.0})")
                    } else {
                        Log.d(Constant.TAG, "SIMULATION: Item-based vending successful: VMC received item $itemNumber")
                    }
                    
                    // Simulate successful response
                    actuator = CashlessAction.ACT_SESSION_COMPLETE
                    handler.sendEmptyMessage(Event.EV_CASHLESS_VEND_APPROVED)
                    
                } catch (e: Exception) {
                    Log.e(Constant.TAG, "Error in simulation vendSuccess: ${e.message}", e)
                }
            }.start()
            return
        }
        
        // Run MDB communication on background thread to avoid blocking UI
        Thread {
            try {
                val cmdBuffer = ByteArray(36)
                val response = ByteArray(36)
                var checksum = 0

                // Send VEND SUCCESS directly (payment already processed in app)
                cmdBuffer[0] = CASHLESS_VEND.toByte()
                cmdBuffer[1] = SUBCMD_VEND_SUCCESS.toByte()
                
                // Check if this is amount-based vending (itemNumber = 0) or item-based vending
                if (itemNumber == 0) {
                    // Amount-based vending: Send user's selected amount to VMC
                    cmdBuffer[2] = ((itemPrice shr 8) and 0xFF).toByte()
                    cmdBuffer[3] = (itemPrice and 0xFF).toByte()
                    
                    Log.d(Constant.TAG, "VEND SUCCESS: Sending amount ${itemPrice} cents ($${itemPrice / 100.0}) to VMC")
                } else {
                    // Item-based vending: Send item number to VMC
                    cmdBuffer[2] = ((itemNumber shr 8) and 0xFF).toByte()
                    cmdBuffer[3] = (itemNumber and 0xFF).toByte()
                    
                    Log.d(Constant.TAG, "VEND SUCCESS: Sending item number $itemNumber to VMC")
                }

                // Calculate checksum
                for (i in 0..3) {
                    checksum += cmdBuffer[i].toInt() and 0xFF
                }
                cmdBuffer[4] = (checksum and 0xFF).toByte()

                // Log the MDB command being sent
                val cmdString = cmdBuffer.take(5).joinToString(" ") { "0x%02X".format(it.toInt() and 0xFF) }
                Log.d(Constant.TAG, "Sending VEND SUCCESS command: $cmdString")

                val ret = sendMdbCommand(cmdBuffer, 5, response)

                if (isTransmissionSuccess(ret) && ret == 1 && response[0].toInt() != Constant.NAK) {
                    actuator = CashlessAction.ACT_SESSION_COMPLETE
                    
                    if (itemNumber == 0) {
                        Log.d(Constant.TAG, "Amount-based vending successful: VMC received ${itemPrice} cents")
                    } else {
                        Log.d(Constant.TAG, "Item-based vending successful: VMC received item $itemNumber")
                    }
                    
                    // Notify that payment was successful and VMC can process the amount/item
                    handler.sendEmptyMessage(Event.EV_CASHLESS_VEND_APPROVED)
                } else {
                    Log.e(Constant.TAG, "VEND SUCCESS command failed: ret=$ret, response=${response[0].toInt() and 0xFF}")
                }
            } catch (e: Exception) {
                Log.e(Constant.TAG, "Error in vendSuccess: ${e.message}", e)
            }
        }.start()
    }

//    private fun vendSuccess() {
//        var checksum = 0
//        val response = ByteArray(36)
//
//        cmdBuffer[0] = CASHLESS_VEND.toByte()
//        cmdBuffer[1] = SUBCMD_VEND_SUCCESS.toByte()
//        cmdBuffer[2] = ((itemNumber shr 8) and 0xFF).toByte()
//        cmdBuffer[3] = (itemNumber and 0xFF).toByte()
//
//        for (i in 0..3) {
//            checksum += cmdBuffer[i].toInt() and 0xFF
//        }
//        cmdBuffer[4] = (checksum and 0xFF).toByte()
//
//        val ret = mdbMaster.sendCommand(cmdBuffer, 5, response)
//        if (!isTransmissionSuccess(ret)) {
//            return
//        }
//
//        // If response size is not 1 that means its not a correct response for VEND SUCCESS
//        // or received NAK - in this case, the command would be re-sent
//        if (ret != 1 || response[0] == Constant.NAK.toByte()) {
//            return
//        }
//
//        // Received ACK, SESSION_COMPLETE would be sent
//        actuator = CashlessAction.ACT_SESSION_COMPLETE
//    }



        // New method for app-processed payments
//        fun sendVendSuccessDirectly(itemNumber: Int, itemPrice: Int) {
//            this.itemNumber = itemNumber
//            this.itemPrice = itemPrice
//            this.actuator = CashlessAction.ACT_VEND_SUCCESS
//        }

        // Modified vendSuccess to handle app payments


    private fun vendFailure() {
        var checksum = 0
        val response = ByteArray(36)
        
        cmdBuffer[0] = CASHLESS_VEND.toByte()
        cmdBuffer[1] = SUBCMD_VEND_FAILURE.toByte()
        checksum = (cmdBuffer[0].toInt() and 0xFF) + (cmdBuffer[1].toInt() and 0xFF)
        cmdBuffer[2] = (checksum and 0xFF).toByte()
        
        val ret = sendMdbCommand(cmdBuffer, 3, response)
        if (!isTransmissionSuccess(ret)) {
            return
        }

        // If response size is not 1 that means its not a correct response for command
        // or received NAK - in this case, the command would be re-sent
        if (ret != 1 || response[0] == Constant.NAK.toByte()) {
            return
        }

        // Product dispensed failure, POLL for refund
        isDispensedFailure = true
        actuator = CashlessAction.ACT_POLL
    }

    private fun sessionComplete() {
        var checksum = 0
        val response = ByteArray(36)
        
        cmdBuffer[0] = CASHLESS_VEND.toByte()
        cmdBuffer[1] = SUBCMD_SESSION_COMPLETE.toByte()
        checksum = (cmdBuffer[0].toInt() and 0xFF) + (cmdBuffer[1].toInt() and 0xFF)
        cmdBuffer[2] = (checksum and 0xFF).toByte()
        
        val ret = sendMdbCommand(cmdBuffer, 3, response)
        if (!isTransmissionSuccess(ret)) {
            return
        }

        // If response size is not 1 that means its not a correct response for command
        // or received NAK - in this case, the command would be re-sent
        if (ret != 1 || response[0] == Constant.NAK.toByte()) {
            return
        }

        // Received ACK, POLL would be sent
        actuator = CashlessAction.ACT_POLL
    }

    override fun process() {
        // In simulation mode, only process specific actions, not continuous polling
        if (mdbMaster == null) {
            when (actuator) {
                CashlessAction.ACT_RESET -> {
                    // Simulate successful reset
                    actuator = CashlessAction.ACT_POLL
                    isOnline = true
                    initializingSequenceFinish = true
                    Log.d(Constant.TAG, "Simulation mode: CashLessDevice reset completed")
                }
                CashlessAction.ACT_VEND_SUCCESS -> vendSuccess()
                CashlessAction.ACT_VEND_CANCEL -> vendCancel()
                CashlessAction.ACT_VEND_FAILURE -> vendFailure()
                CashlessAction.ACT_SESSION_COMPLETE -> sessionComplete()
                // Skip continuous polling in simulation mode
                CashlessAction.ACT_POLL -> {
                    // Do nothing in simulation mode to prevent infinite loop
                }
                else -> {
                    // For other actions, just log and set to POLL
                    Log.d(Constant.TAG, "Simulation mode: Skipping action $actuator")
                    actuator = CashlessAction.ACT_POLL
                }
            }
            return
        }
        
        // Real hardware mode - process normally
        when (actuator) {
            CashlessAction.ACT_RESET -> reset()
            CashlessAction.ACT_POLL -> poll()
            CashlessAction.ACT_SETUP_CONFIG_DATA -> setupConfigData()
            CashlessAction.ACT_SETUP_MAXMIN_PRICES -> setupMaxMinPrices()
            CashlessAction.ACT_EXPANSION_REQUEST_ID -> expansionRequestId()
            CashlessAction.ACT_EXPANSION_OPTION_FEATURE_ENABLE -> expansionOptionEnable()
            CashlessAction.ACT_READER_ENABLE -> readerEnable()
            CashlessAction.ACT_VEND_REQUEST -> vendRequest()
            CashlessAction.ACT_VEND_CANCEL -> vendCancel()
            CashlessAction.ACT_VEND_SUCCESS -> vendSuccess()
            CashlessAction.ACT_VEND_FAILURE -> vendFailure()
            CashlessAction.ACT_SESSION_COMPLETE -> sessionComplete()
            else -> {
                // Handle other actions if needed
            }
        }
    }

    fun setActuator(actuator: CashlessAction) {
        this.actuator = actuator
    }

    fun clean() {
        maxNonRespTimerCancel()
        initializingSequenceFinish = false
        actuator = CashlessAction.ACT_RESET
    }

    fun execVendRequestItem(itemPrice: Int, itemNumber: Int) {
        this.itemPrice = itemPrice
        this.itemNumber = itemNumber
        this.actuator = CashlessAction.ACT_VEND_REQUEST
    }

    fun execCancel(item: Int) {
        if (item != 0) {
            // Cashless in Vend state, cancel by VEND CANCEL
            this.actuator = CashlessAction.ACT_VEND_CANCEL
        } else {
            // Cashless in Session idle state, cancel by SESSION COMPLETE
            this.actuator = CashlessAction.ACT_SESSION_COMPLETE
        }
    }

    fun execDispensedSuccess(itemNumber: Int) {
        this.actuator = CashlessAction.ACT_VEND_SUCCESS
        this.itemNumber = itemNumber
    }

    fun execDispensedSuccessWithAmount(amount: Int) {
        Log.d(Constant.TAG, "execDispensedSuccessWithAmount called with amount: $amount cents")
//        this.actuator = CashlessAction.ACT_VEND_SUCCESS
        this.itemPrice = amount
        // Set itemNumber to 0 to indicate amount-based vending
        this.itemNumber = 0
        
        Log.d(Constant.TAG, "Amount-based vending: User paid $amount cents ($${amount / 100.0}), sending to VMC")
    }

    fun execDispensedFailure() {
        this.actuator = CashlessAction.ACT_VEND_FAILURE
    }
}