package com.example.mdbbill.payment

import android.content.Context
import android.util.Log
import com.example.mdbbill.data.PaymentProcessor
import com.mypos.smartsdk.MyPOSAPI
import com.mypos.smartsdk.MyPOSPayment
import com.mypos.smartsdk.MyPOSUtil
import com.mypos.smartsdk.Currency
import com.mypos.smartsdk.TransactionProcessingResult

interface PaymentGateway {
    fun initialize(context: Context)
    fun startPayment(amount: Double, onResult: (Boolean, String) -> Unit)
    fun isInitialized(): Boolean
}

// myPOS Implementation
class MyPosGateway : PaymentGateway {
    private var initialized = false
    private var context: Context? = null
    
    override fun initialize(context: Context) {
        this.context = context
        try {
            // Initialize myPOS SDK
            Log.d("MyPosGateway", "Initializing myPOS SDK...")
            initialized = true
        } catch (e: Exception) {
            Log.e("MyPosGateway", "Failed to initialize myPOS SDK", e)
            initialized = false
        }
    }
    
    override fun startPayment(amount: Double, onResult: (Boolean, String) -> Unit) {
        if (!initialized || context == null) {
            onResult(false, "myPOS SDK not initialized")
            return
        }
        
        try {
            Log.d("MyPosGateway", "Starting myPOS payment for amount: $amount")
            
            // Build the payment request
            val payment = MyPOSPayment.builder()
                .productAmount(amount)
                .currency(Currency.EUR)
                .foreignTransactionId(java.util.UUID.randomUUID().toString())
                .printMerchantReceipt(MyPOSUtil.RECEIPT_ON)
                .printCustomerReceipt(MyPOSUtil.RECEIPT_ON)
                .mastercardSonicBranding(true)
                .visaSensoryBranding(true)
                .fixedPinpad(true)
                .build()
            
            // Start the payment activity
            if (context is android.app.Activity) {
                // Store the callback for later use
                paymentCallback = onResult
                MyPOSAPI.openPaymentActivity(context as android.app.Activity, payment, PAYMENT_REQUEST_CODE)
            } else {
                onResult(false, "Context is not an Activity")
            }
        } catch (e: Exception) {
            Log.e("MyPosGateway", "Payment failed", e)
            onResult(false, "Payment failed: ${e.message}")
        }
    }

    // New method that accepts Activity context directly
    fun startPaymentWithActivity(activity: android.app.Activity, amount: Double, onResult: (Boolean, String) -> Unit) {
        if (!initialized) {
            onResult(false, "myPOS SDK not initialized")
            return
        }
        
        try {
            Log.d("MyPosGateway", "Starting myPOS payment for amount: $amount")
            
            // Build the payment request
            val payment = MyPOSPayment.builder()
                .productAmount(amount)
                .currency(Currency.EUR)
                .foreignTransactionId(java.util.UUID.randomUUID().toString())
                .printMerchantReceipt(MyPOSUtil.RECEIPT_ON)
                .printCustomerReceipt(MyPOSUtil.RECEIPT_ON)
                .mastercardSonicBranding(true)
                .visaSensoryBranding(true)
                .fixedPinpad(true)
                .build()
            
            // Store the callback for later use
            paymentCallback = onResult
            
            // Check if myPOS app is available
            try {
                MyPOSAPI.openPaymentActivity(activity, payment, PAYMENT_REQUEST_CODE)
                Log.d("MyPosGateway", "myPOS payment activity launched successfully")
            } catch (e: Exception) {
                Log.e("MyPosGateway", "Failed to launch myPOS payment activity", e)
                onResult(false, "myPOS app not available or not installed")
            }
        } catch (e: Exception) {
            Log.e("MyPosGateway", "Payment failed", e)
            onResult(false, "Payment failed: ${e.message}")
        }
    }
    
    override fun isInitialized(): Boolean = initialized
    
    companion object {
        const val PAYMENT_REQUEST_CODE = 1001
        var paymentCallback: ((Boolean, String) -> Unit)? = null
        
        // Method to check if myPOS app is available on the device
        fun isMyPosAvailable(context: Context): Boolean {
            return try {
                val packageManager = context.packageManager
                packageManager.getPackageInfo("com.mypos", 0)
                true
            } catch (e: Exception) {
                Log.w("MyPosGateway", "myPOS app not found on device", e)
                false
            }
        }
        
        fun handlePaymentResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
            if (requestCode == PAYMENT_REQUEST_CODE) {
                Log.d("MyPosGateway", "Payment result received - ResultCode: $resultCode")
                
                if (resultCode == android.app.Activity.RESULT_OK) {
                    if (data == null) {
                        Log.w("MyPosGateway", "Payment result data is null")
                        paymentCallback?.invoke(false, "Transaction cancelled")
                        return
                    }
                    
                    val transactionResult = data.getIntExtra("status", TransactionProcessingResult.TRANSACTION_FAILED)
                    val transactionApproved = data.getBooleanExtra("transaction_approved", false)
                    val responseCode = data.getStringExtra("response_code") ?: "Unknown"
                    val cardBrand = data.getStringExtra("card_brand") ?: "Unknown"
                    val authorizationCode = data.getStringExtra("authorization_code") ?: "N/A"
                    
                    Log.d("MyPosGateway", "Transaction result: $transactionResult, Approved: $transactionApproved, Response: $responseCode")
                    
                    if (transactionResult == TransactionProcessingResult.TRANSACTION_SUCCESS && transactionApproved) {
                        val amount = data.getDoubleExtra("amount", 0.0)
                        val message = "Payment successful (Code: $responseCode, Card: $cardBrand, Auth: $authorizationCode)"
                        Log.i("MyPosGateway", message)
                        paymentCallback?.invoke(true, message)
                    } else {
                        val message = "Payment declined (Code: $responseCode)"
                        Log.w("MyPosGateway", message)
                        paymentCallback?.invoke(false, message)
                    }
                } else {
                    Log.d("MyPosGateway", "Payment cancelled by user")
                    paymentCallback?.invoke(false, "Transaction cancelled by user")
                }
                paymentCallback = null
            }
        }
    }
}

// Revolut Implementation
class RevolutGateway : PaymentGateway {
    private var initialized = false
    
    override fun initialize(context: Context) {
        try {
            // TODO: Initialize Revolut SDK with actual implementation
            // For now, we'll simulate initialization
            Log.d("RevolutGateway", "Initializing Revolut SDK...")
            initialized = true
        } catch (e: Exception) {
            Log.e("RevolutGateway", "Failed to initialize Revolut SDK", e)
            initialized = false
        }
    }
    
    override fun startPayment(amount: Double, onResult: (Boolean, String) -> Unit) {
        if (!initialized) {
            onResult(false, "Revolut SDK not initialized")
            return
        }
        
        try {
            // TODO: Call Revolut SDK payment intent
            // For now, we'll simulate a successful payment
            Log.d("RevolutGateway", "Starting Revolut payment for amount: $amount")
            
            // Simulate payment processing
            Thread {
                Thread.sleep(2000) // Simulate processing time
                onResult(true, "Payment successful")
            }.start()
        } catch (e: Exception) {
            Log.e("RevolutGateway", "Payment failed", e)
            onResult(false, "Payment failed: ${e.message}")
        }
    }
    
    override fun isInitialized(): Boolean = initialized
}

// Payment Manager to handle gateway selection
class PaymentManager(private val context: Context) {
    private val myposGateway = MyPosGateway()
    private val revolutGateway = RevolutGateway()
    
    init {
        myposGateway.initialize(context)
        revolutGateway.initialize(context)
    }
    
    fun getGateway(processor: PaymentProcessor): PaymentGateway {
        return when (processor) {
            PaymentProcessor.MYPOS -> myposGateway
            PaymentProcessor.REVOLUT -> revolutGateway
        }
    }
} 