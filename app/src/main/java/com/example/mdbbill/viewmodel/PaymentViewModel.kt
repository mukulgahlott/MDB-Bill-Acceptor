package com.example.mdbbill.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mdbbill.data.AppSettings
import com.example.mdbbill.data.OperatingMode
import com.example.mdbbill.data.PaymentProcessor
import com.example.mdbbill.mdb.MdbController
import com.example.mdbbill.mdb.VendingMachineController
import com.example.mdbbill.payment.PaymentManager
import com.example.mdbbill.payment.MyPosGateway
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaymentViewModel(application: Application) : AndroidViewModel(application) {
    private val appSettings = AppSettings(application)
    private val paymentManager = PaymentManager(application)
    private val mdbController = MdbController(application)
    private val vmController = VendingMachineController.getInstance()

    private val _selectedAmount = MutableStateFlow(0.0f)
    val selectedAmount: StateFlow<Float> = _selectedAmount.asStateFlow()

    private val _customAmount = MutableStateFlow("")
    val customAmount: StateFlow<String> = _customAmount.asStateFlow()

    private val _isPaymentProcessing = MutableStateFlow(false)
    val isPaymentProcessing: StateFlow<Boolean> = _isPaymentProcessing.asStateFlow()

    // Track current item for vending machine operations (optional)
    private var currentItemNumber: Int = 0

    private val _paymentResult = MutableStateFlow<PaymentResult?>(null)
    val paymentResult: StateFlow<PaymentResult?> = _paymentResult.asStateFlow()

    fun setSelectedAmount(amount: Float) {
        _selectedAmount.value = amount
    }

    fun setCustomAmount(amount: String) {
        _customAmount.value = amount
    }

    fun setCurrentItem(itemNumber: Int) {
        currentItemNumber = itemNumber
    }

    fun processPayment() {
        val amount = if (_selectedAmount.value > 0) {
            _selectedAmount.value.toDouble()
        } else {
            _customAmount.value.toDoubleOrNull() ?: 0.0
        }

        if (amount <= 0) {
            _paymentResult.value = PaymentResult.Error("Invalid amount")
            return
        }

        if (amount < appSettings.minimumAmount) {
            _paymentResult.value = PaymentResult.Error("Amount below minimum (${appSettings.minimumAmount})")
            return
        }

        if (amount > appSettings.maximumAmount) {
            _paymentResult.value = PaymentResult.Error("Amount above maximum (${appSettings.maximumAmount})")
            return
        }

        if (OperatingMode.TRAINING == appSettings.operatingMode){
            // Call vendSuccess to notify VMC that payment was successful (send amount)
            val amountInCents = (amount * 100).toInt()
            android.util.Log.d("PaymentViewModel", "Training mode: Processing payment of $amountInCents cents")
            
            val cashLessDevice = vmController.getCashLessDevice()
            if (cashLessDevice != null) {
                android.util.Log.d("PaymentViewModel", "CashLessDevice found, calling execDispensedSuccessWithAmount")
                cashLessDevice.execDispensedSuccessWithAmount(amountInCents)
                android.util.Log.d("PaymentViewModel", "Calling vendSuccess()")
                cashLessDevice.vendSuccess()
            } else {
                android.util.Log.e("PaymentViewModel", "CashLessDevice is null!")
            }
            
            _paymentResult.value = PaymentResult.Success(amount, "AMOUNT SUCCESS")
        } else {
            viewModelScope.launch {
                _isPaymentProcessing.value = true
                _paymentResult.value = null

                try {
                    val gateway = paymentManager.getGateway(appSettings.paymentProcessor)

                    // For myPOS, we need to handle this differently since it requires Activity context
                    if (appSettings.paymentProcessor == PaymentProcessor.MYPOS) {
                        // Store the amount and trigger the payment request
                        _pendingPaymentAmount.value = amount
                        _shouldLaunchPayment.value = true
                    } else {
                        // For other gateways, use the normal flow
                        gateway.startPayment(amount) { success, message ->
                            viewModelScope.launch {
                                if (success) {
                                    // Call vendSuccess to notify VMC that payment was successful (send amount)
                                    val amountInCents = (amount * 100).toInt()
                                    android.util.Log.d("PaymentViewModel", "Other gateway success: Processing payment of $amountInCents cents")
                                    
                                    val cashLessDevice = vmController.getCashLessDevice()
                                    if (cashLessDevice != null) {
                                        android.util.Log.d("PaymentViewModel", "CashLessDevice found, calling execDispensedSuccessWithAmount")
                                        cashLessDevice.execDispensedSuccessWithAmount(amountInCents)
                                        android.util.Log.d("PaymentViewModel", "Calling vendSuccess()")
                                        cashLessDevice.vendSuccess()
                                    } else {
                                        android.util.Log.e("PaymentViewModel", "CashLessDevice is null!")
                                    }
                                    
                                    _paymentResult.value = PaymentResult.Success(amount, message)
                                } else {
                                    _paymentResult.value = PaymentResult.Error(message)
                                }
                                _isPaymentProcessing.value = false
                            }
                        }
                    }
                } catch (e: Exception) {
                    _paymentResult.value = PaymentResult.Error("Payment failed: ${e.message}")
                    _isPaymentProcessing.value = false
                }
            }
        }
    }

    // New state flows for myPOS payment handling
    private val _pendingPaymentAmount = MutableStateFlow(0.0)
    val pendingPaymentAmount: StateFlow<Double> = _pendingPaymentAmount.asStateFlow()

    private val _shouldLaunchPayment = MutableStateFlow(false)
    val shouldLaunchPayment: StateFlow<Boolean> = _shouldLaunchPayment.asStateFlow()

    // Method to be called from Activity to launch myPOS payment
    fun launchMyPosPayment(activity: android.app.Activity) {
        val amount = _pendingPaymentAmount.value
        if (amount > 0) {
            try {
                val gateway = paymentManager.getGateway(appSettings.paymentProcessor) as MyPosGateway
                
                // Check if myPOS app is available
                if (!MyPosGateway.isMyPosAvailable(activity)) {
                    _paymentResult.value = PaymentResult.Error("myPOS app not installed on this device")
                    _isPaymentProcessing.value = false
                    _shouldLaunchPayment.value = false
                    _pendingPaymentAmount.value = 0.0
                    return
                }
                
                gateway.startPaymentWithActivity(activity, amount) { success, message ->
                    viewModelScope.launch {
                        if (success) {
                            // Call vendSuccess to notify VMC that payment was successful (send amount)
                            val amountInCents = (amount * 100).toInt()
                            android.util.Log.d("PaymentViewModel", "myPOS success: Processing payment of $amountInCents cents")
                            
                            val cashLessDevice = vmController.getCashLessDevice()
                            if (cashLessDevice != null) {
                                android.util.Log.d("PaymentViewModel", "CashLessDevice found, calling execDispensedSuccessWithAmount")
                                cashLessDevice.execDispensedSuccessWithAmount(amountInCents)
                                android.util.Log.d("PaymentViewModel", "Calling vendSuccess()")
                                cashLessDevice.vendSuccess()
                            } else {
                                android.util.Log.e("PaymentViewModel", "CashLessDevice is null!")
                            }
                            
                            _paymentResult.value = PaymentResult.Success(amount, message)
                        } else {
                            _paymentResult.value = PaymentResult.Error(message)
                        }
                        _isPaymentProcessing.value = false
                        _shouldLaunchPayment.value = false
                        _pendingPaymentAmount.value = 0.0
                    }
                }
            } catch (e: Exception) {
                _paymentResult.value = PaymentResult.Error("Payment failed: ${e.message}")
                _isPaymentProcessing.value = false
                _shouldLaunchPayment.value = false
                _pendingPaymentAmount.value = 0.0
            }
        }
    }

    fun clearPaymentResult() {
        _paymentResult.value = null
    }

    fun resetAmount() {
        _selectedAmount.value = 0.0f
        _customAmount.value = ""
    }
}

sealed class PaymentResult {
    data class Success(val amount: Double, val message: String) : PaymentResult()
    data class Error(val message: String) : PaymentResult()
} 