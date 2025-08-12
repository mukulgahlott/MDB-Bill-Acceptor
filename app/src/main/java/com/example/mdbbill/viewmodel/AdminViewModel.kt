package com.example.mdbbill.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mdbbill.data.AppSettings
import com.example.mdbbill.data.OperatingMode
import com.example.mdbbill.data.PaymentProcessor
import com.example.mdbbill.mdb.MdbController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(application: Application) : AndroidViewModel(application) {
    private val appSettings = AppSettings(application)
    private val mdbController = MdbController(application)

    private val _operatingMode = MutableStateFlow(appSettings.operatingMode)
    val operatingMode: StateFlow<OperatingMode> = _operatingMode.asStateFlow()

    private val _paymentProcessor = MutableStateFlow(appSettings.paymentProcessor)
    val paymentProcessor: StateFlow<PaymentProcessor> = _paymentProcessor.asStateFlow()

    private val _predefinedAmount1 = MutableStateFlow(appSettings.predefinedAmount1)
    val predefinedAmount1: StateFlow<Float> = _predefinedAmount1.asStateFlow()

    private val _predefinedAmount2 = MutableStateFlow(appSettings.predefinedAmount2)
    val predefinedAmount2: StateFlow<Float> = _predefinedAmount2.asStateFlow()

    private val _predefinedAmount3 = MutableStateFlow(appSettings.predefinedAmount3)
    val predefinedAmount3: StateFlow<Float> = _predefinedAmount3.asStateFlow()

    private val _minimumAmount = MutableStateFlow(appSettings.minimumAmount)
    val minimumAmount: StateFlow<Float> = _minimumAmount.asStateFlow()

    private val _maximumAmount = MutableStateFlow(appSettings.maximumAmount)
    val maximumAmount: StateFlow<Float> = _maximumAmount.asStateFlow()

    private val _myposApiKey = MutableStateFlow(appSettings.myposApiKey)
    val myposApiKey: StateFlow<String> = _myposApiKey.asStateFlow()

    private val _revolutApiKey = MutableStateFlow(appSettings.revolutApiKey)
    val revolutApiKey: StateFlow<String> = _revolutApiKey.asStateFlow()

    private val _adminPin = MutableStateFlow(appSettings.adminPin)
    val adminPin: StateFlow<String> = _adminPin.asStateFlow()

    private val _isTestConnectionLoading = MutableStateFlow(false)
    val isTestConnectionLoading: StateFlow<Boolean> = _isTestConnectionLoading.asStateFlow()

    private val _testConnectionResult = MutableStateFlow<String?>(null)
    val testConnectionResult: StateFlow<String?> = _testConnectionResult.asStateFlow()

    fun setMode(mode: OperatingMode) {
        appSettings.operatingMode = mode
        _operatingMode.value = mode
    }

    fun setProcessor(processor: PaymentProcessor) {
        appSettings.paymentProcessor = processor
        _paymentProcessor.value = processor
    }

    fun setPredefinedAmount1(amount: Float) {
        appSettings.predefinedAmount1 = amount
        _predefinedAmount1.value = amount
    }

    fun setPredefinedAmount2(amount: Float) {
        appSettings.predefinedAmount2 = amount
        _predefinedAmount2.value = amount
    }

    fun setPredefinedAmount3(amount: Float) {
        appSettings.predefinedAmount3 = amount
        _predefinedAmount3.value = amount
    }

    fun setMinimumAmount(amount: Float) {
        appSettings.minimumAmount = amount
        _minimumAmount.value = amount
    }

    fun setMaximumAmount(amount: Float) {
        appSettings.maximumAmount = amount
        _maximumAmount.value = amount
    }

    fun setMyposApiKey(key: String) {
        appSettings.myposApiKey = key
        _myposApiKey.value = key
    }

    fun setRevolutApiKey(key: String) {
        appSettings.revolutApiKey = key
        _revolutApiKey.value = key
    }

    fun setAdminPin(pin: String) {
        appSettings.adminPin = pin
        _adminPin.value = pin
    }

//    fun testConnection() {
//        viewModelScope.launch {
//            _isTestConnectionLoading.value = true
//            _testConnectionResult.value = null
//
//            try {
//                val result = mdbController.testConnection()
//                _testConnectionResult.value = if (result) "Connection successful" else "Connection failed"
//            } catch (e: Exception) {
//                _testConnectionResult.value = "Connection error: ${e.message}"
//            } finally {
//                _isTestConnectionLoading.value = false
//            }
//        }
//    }

    fun resetApp() {
        appSettings.resetAllSettings()
        // Reload all values from settings
        _operatingMode.value = appSettings.operatingMode
        _paymentProcessor.value = appSettings.paymentProcessor
        _predefinedAmount1.value = appSettings.predefinedAmount1
        _predefinedAmount2.value = appSettings.predefinedAmount2
        _predefinedAmount3.value = appSettings.predefinedAmount3
        _minimumAmount.value = appSettings.minimumAmount
        _maximumAmount.value = appSettings.maximumAmount
        _myposApiKey.value = appSettings.myposApiKey
        _revolutApiKey.value = appSettings.revolutApiKey
        _adminPin.value = appSettings.adminPin
    }

    fun clearTestResult() {
        _testConnectionResult.value = null
    }
} 