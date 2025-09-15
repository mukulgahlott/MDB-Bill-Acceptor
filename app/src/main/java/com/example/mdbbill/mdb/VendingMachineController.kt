package com.example.mdbbill.mdb

import android.hardware.mdbMaster.MdbMaster
import android.os.Handler
import android.util.Log
import com.example.mdbbill.peripheral.BillValidator
import com.example.mdbbill.peripheral.CashLessDevice
import com.example.mdbbill.peripheral.CoinChanger
import com.example.mdbbill.peripheral.Constant
import kotlinx.coroutines.*

class VendingMachineController private constructor() : Thread("VMController Thread") {
    
    companion object {
        private var instance: VendingMachineController? = null
        
        fun getInstance(): VendingMachineController {
            if (instance == null) {
                instance = VendingMachineController()
            }
            return instance!!
        }
    }

    /**VMC product information*/
    val vmcFeatureLevel = 2
    val manufacturerCode = byteArrayOf('c'.toByte(), 't'.toByte(), 'k'.toByte())
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
        
        // Open MDB Master connection
        if (mdbMaster?.open() != MdbMaster.SUCCESS) {
            return false
        }
        
        // Initialize payment devices
        if (coinChangerSupport) {
            coinChanger = CoinChanger(mdbMaster!!, handler)
        }
        if (billValidatorSupport) {
            billValidator = BillValidator(mdbMaster!!, handler)
        }
        if (cashlessDeviceSupport) {
            cashLessDevice = CashLessDevice(mdbMaster!!, handler)
        }

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

    fun destroy() {
        isRunning = false
        try {
            join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        instance = null
    }
}
