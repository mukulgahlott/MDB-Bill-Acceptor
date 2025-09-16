package com.example.mdbbill.mdb

import android.hardware.mdbMaster.MdbMaster
import android.os.Handler
import android.util.Log
import com.example.mdbbill.peripheral.BillValidator
import com.example.mdbbill.peripheral.CashLessDevice
import com.example.mdbbill.peripheral.CoinChanger
import com.example.mdbbill.peripheral.Constant

class VendingMachineController() : Thread("VMController Thread") {
    
    companion object {
        private var instance: VendingMachineController? = null
        
        fun getInstance(): VendingMachineController {
            if (instance == null) {
                instance = VendingMachineController()
            }
            return instance!!
        }
        
        fun createNewInstance(): VendingMachineController {
            // Always create a new instance
            return VendingMachineController()
        }
    }

    /**VMC product information*/
    val vmcFeatureLevel = 2
    val manufacturerCode = byteArrayOf('c'.code.toByte(), 't'.code.toByte(), 'k'.code.toByte())
    val serialNumber = "ctk-vmc12345".toByteArray()
    val modelNumber = "cm30-vmc1234".toByteArray()
    val softwareVersion = "10".toByteArray()

    val cashlessDeviceSupport = true
    val coinChangerSupport = true
    val billValidatorSupport = true

    private var handler: Handler? = null
    private var isRunning = false
    private var mdbMaster: MdbMaster? = null
    private var coinChanger: CoinChanger? = null
    private var billValidator: BillValidator? = null
    private var cashLessDevice: CashLessDevice? = null

    private val vmcLock = Any()

    private val POLL_INTERVAL_TIMEOUT = 100L // 100ms

    init {
        // Initialize MDB Master
        mdbMaster = MdbMaster.getInstance()
    }

    fun executeItemSelected(itemNumber: Int, itemPrice: Int) {
        synchronized(vmcLock) {
            cashLessDevice?.execVendRequestItem(itemNumber, itemPrice)
        }
    }

    fun executeCancelVend(item: Int) {
        synchronized(vmcLock) {
            cashLessDevice?.execCancel(item)
        }
    }

    fun initialize(handler: Handler): Boolean {
        this.handler = handler
        
        Log.d("VendingMachineController", "Initializing VendingMachineController...")
        Log.d("VendingMachineController", "mdbMaster instance: $mdbMaster")
        
        // Check if MdbMaster is available
        if (mdbMaster == null) {
            Log.e("VendingMachineController", "MdbMaster.getInstance() returned null - CM30 SDK not properly integrated")
            Log.e("VendingMachineController", "Please check: 1) CM30 SDK is added to project, 2) Hardware permissions, 3) Device compatibility")
            return false
        }
        
        // Try to open MDB Master connection
        Log.d("VendingMachineController", "Attempting to open MDB Master connection...")
        val openResult = mdbMaster!!.open()
        Log.d("VendingMachineController", "MDB Master open() result: $openResult")
        Log.d("VendingMachineController", "MdbMaster.SUCCESS constant: ${MdbMaster.SUCCESS}")
        
        if (openResult != MdbMaster.SUCCESS) {
            Log.w("VendingMachineController", "MDB Master connection failed. Result: $openResult")
            Log.w("VendingMachineController", "Possible causes:")
            Log.w("VendingMachineController", "1. CM30 hardware not connected")
            Log.w("VendingMachineController", "2. USB OTG not enabled")
            Log.w("VendingMachineController", "3. Hardware drivers not installed")
            Log.w("VendingMachineController", "4. Vending machine not connected to CM30")
            Log.w("VendingMachineController", "5. MDB communication not established")
            Log.w("VendingMachineController", "Continuing with simulated MDB devices for testing...")
            
            // Create simulated devices for testing when hardware is not available
            if (cashlessDeviceSupport) {
                cashLessDevice = CashLessDevice(null, handler) // Pass null for mdbMaster to indicate simulation mode
                Log.d("VendingMachineController", "CashLessDevice initialized in simulation mode")
            }
            
            Log.d("VendingMachineController", "VendingMachineController initialization completed (simulation mode)")
            return true
        }
        
        Log.d("VendingMachineController", "MDB Master connection opened successfully - CM30 hardware detected!")
        Log.d("VendingMachineController", "Initializing real MDB devices...")
        
        // Initialize payment devices with real hardware
        if (coinChangerSupport) {
            coinChanger = CoinChanger(mdbMaster!!, handler)
            Log.d("VendingMachineController", "CoinChanger initialized with real hardware")
        }
        if (billValidatorSupport) {
            billValidator = BillValidator(mdbMaster!!, handler)
            Log.d("VendingMachineController", "BillValidator initialized with real hardware")
        }
        if (cashlessDeviceSupport) {
            cashLessDevice = CashLessDevice(mdbMaster!!, handler)
            Log.d("VendingMachineController", "CashLessDevice initialized with real hardware")
        }

        Log.d("VendingMachineController", "VendingMachineController initialization completed with real hardware")
        return true
    }

    override fun run() {
        if (coinChanger == null && billValidator == null && cashLessDevice == null) {
            return
        }

        isRunning = true
        
        // Continuous polling loop
        while (isRunning) {
            if (coinChangerSupport) {
                coinChanger?.process()
                Constant.delay(POLL_INTERVAL_TIMEOUT.toInt())
            }

            if (billValidatorSupport) {
                billValidator?.process()
                Constant.delay(POLL_INTERVAL_TIMEOUT.toInt())
            }

            if (cashlessDeviceSupport) {
                cashLessDevice?.process()
                Constant.delay(POLL_INTERVAL_TIMEOUT.toInt())
            }
        }

        Log.d(Constant.TAG, "VMC thread finish")
        mdbMaster?.close()
    }

    fun isThreadRunning(): Boolean {
        return isAlive && isRunning
    }
    
    fun startIfNotRunning() {
        if (!isAlive) {
            Log.d("VendingMachineController", "Starting new VMC thread...")
            start()
        } else if (isRunning) {
            Log.d("VendingMachineController", "VMC thread already running, skipping start")
        } else {
            Log.d("VendingMachineController", "VMC thread finished, cannot restart - this should not happen with proper lifecycle management")
            throw IllegalStateException("VMC thread has finished and cannot be restarted. Use proper lifecycle management.")
        }
    }

    fun destroy() {
        Log.d("VendingMachineController", "Destroying VMC - stopping thread...")
        isRunning = false
        try {
            if (isAlive) {
                join(2000) // Wait up to 2 seconds for thread to finish
                if (isAlive) {
                    Log.w("VendingMachineController", "Thread did not stop gracefully, interrupting...")
                    interrupt()

                    join(1000) // Wait another second after interrupt
                }
            }
        } catch (e: InterruptedException) {
            Log.w("VendingMachineController", "Thread interrupted during destroy", e)
        }
        
        // Close MDB master connection
        try {
            mdbMaster?.close()
        } catch (e: Exception) {
            Log.e("VendingMachineController", "Error closing MDB master", e)
        }
        
        Log.d("VendingMachineController", "VMC destroyed successfully")
        instance = null
    }

    /**
     * Get the cashless device for external operations
     */
    fun getCashLessDevice(): CashLessDevice? {
        Log.d("VendingMachineController", "getCashLessDevice() called - cashLessDevice is ${if (cashLessDevice != null) "not null" else "null"}")
        return cashLessDevice
    }
}
