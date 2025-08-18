package com.example.mdbbill.mdb

import android.content.Context
import android.hardware.mdbSlave.MdbSlave
import android.util.Log
import com.example.mdbbill.data.AppSettings
import com.example.mdbbill.data.OperatingMode
import java.lang.UnsatisfiedLinkError

class MdbController(private val context: Context) {
    private val appSettings = AppSettings(context)

    private val slave: MdbSlave? by lazy { 
        try {
            MdbSlave.getInstance()
        } catch (e: UnsatisfiedLinkError) {
            Log.e("MdbController", "Native library not available for this architecture: ${e.message}")
            null
        }
    }

    
    // TODO: Replace with actual CM30-HardwareLibrary implementation
    // private val mdbController = MdbController.getInstance() // From CM30-HardwareLibrary
    
    fun onPaymentSuccess(amount: Double) {
        if (slave == null) {
            Log.w("MdbController", "Native library not available - simulating MDB communication")
            val amountInCents = (amount * 100).toInt()
            Log.d("MdbController", "SIMULATION: Sending $amountInCents cents to VMC")
            Log.d("MdbController", "SIMULATION: ACK response sent")
            return
        }
        
        slave?.open()
        val amountInCents = (amount * 100).toInt()
        
        if (appSettings.operatingMode == OperatingMode.LIVE) {
            try {
                // TODO: Implement actual MDB communication using CM30-HardwareLibrary
                sendAmountInCents(amountInCents)
                (slave?.sendAnswer(0x00) ?: 0) > 0

                // For now, we'll simulate the MDB communication
                Log.d("MdbController", "LIVE MODE: Sending $amountInCents cents to VMC")
                Log.d("MdbController", "LIVE MODE: Sending ACK response")
                
                // Simulate MDB communication delay
                Thread {
                    Thread.sleep(500)
                    Log.d("MdbController", "LIVE MODE: MDB communication completed successfully")
                }.start()
                
            } catch (e: Exception) {
                Log.e("MdbController", "MDB communication error", e)
            }
        } else {
            // TRAINING mode: Just log that it would have sent the credit
            sendAmountInCents(amountInCents)
            (slave?.sendAnswer(0x00) ?: 0) > 0
            Log.d("MdbController", "TRAINING MODE: Simulated sending $amountInCents cents to VMC")
            Log.d("MdbController", "TRAINING MODE: Simulated ACK response")
        }
    }
    
    fun testConnection(): Boolean {
        return try {
            // TODO: Implement actual connection test using CM30-HardwareLibrary
            // For now, we'll simulate a successful connection test
            Log.d("MdbController", "Testing MDB connection...")
            Thread.sleep(1000) // Simulate connection test
            Log.d("MdbController", "MDB connection test successful")
            true
        } catch (e: Exception) {
            Log.e("MdbController", "MDB connection test failed", e)
            false
        }
    }
    
    fun initializeMdb() {
        try {
            // TODO: Initialize MDB controller using CM30-HardwareLibrary
            Log.d("MdbController", "Initializing MDB controller...")
            // mdbController.initialize()
            Log.d("MdbController", "MDB controller initialized successfully")
        } catch (e: Exception) {
            Log.e("MdbController", "Failed to initialize MDB controller", e)
        }
    }

    fun sendAmountInCents(amountInCents: Int): Boolean {
        val frame = byteArrayOf(
            0xA1.toByte(), // pretend command id
            ((amountInCents shr 8) and 0xFF).toByte(),
            (amountInCents and 0xFF).toByte()
        )
        return sendResponseData(frame)
    }


    fun sendResponseData(payload: ByteArray): Boolean {
        require(payload.size <= 36) { "MDB limit is 36 bytes" }
        
        val currentSlave = slave
        if (currentSlave == null) {
            Log.w("MdbController", "Native library not available - simulating sendResponseData")
            return true
        }
        
        val resp = IntArray(1)
        val written = currentSlave.sendResponseData(payload, payload.size, resp)
        return written > 0 && resp[0] == 0x00
    }

    fun sendAnswerAck(): Boolean = slave?.sendAnswer(0x00) ?: 0 > 0
} 