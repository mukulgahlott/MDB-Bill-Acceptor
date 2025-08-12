package com.example.mdbbill.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

// Define enums for clarity
enum class OperatingMode { LIVE, TRAINING }
enum class PaymentProcessor { MYPOS, REVOLUT }

class AppSettings(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("MDB_APP_PREFS", Context.MODE_PRIVATE)

    var adminPin: String
        get() = prefs.getString("ADMIN_PIN", "1234") ?: "1234" // Default PIN
        set(value) = prefs.edit { putString("ADMIN_PIN", value) }

    var operatingMode: OperatingMode
        get() = OperatingMode.valueOf(prefs.getString("MODE", OperatingMode.TRAINING.name) ?: OperatingMode.TRAINING.name)
        set(value) = prefs.edit { putString("MODE", value.name) }

    var paymentProcessor: PaymentProcessor
        get() = PaymentProcessor.valueOf(prefs.getString("PROCESSOR", PaymentProcessor.MYPOS.name) ?: PaymentProcessor.MYPOS.name)
        set(value) = prefs.edit { putString("PROCESSOR", value.name) }
    
    // Predefined amounts
    var predefinedAmount1: Float
        get() = prefs.getFloat("PREDEF_AMOUNT_1", 5.0f)
        set(value) = prefs.edit { putFloat("PREDEF_AMOUNT_1", value) }
    
    var predefinedAmount2: Float
        get() = prefs.getFloat("PREDEF_AMOUNT_2", 10.0f)
        set(value) = prefs.edit { putFloat("PREDEF_AMOUNT_2", value) }
    
    var predefinedAmount3: Float
        get() = prefs.getFloat("PREDEF_AMOUNT_3", 20.0f)
        set(value) = prefs.edit { putFloat("PREDEF_AMOUNT_3", value) }

    // Minimum amount
    var minimumAmount: Float
        get() = prefs.getFloat("MIN_AMOUNT", 1.0f)
        set(value) = prefs.edit { putFloat("MIN_AMOUNT", value) }
    
    // Maximum amount
    var maximumAmount: Float
        get() = prefs.getFloat("MAX_AMOUNT", 100.0f)
        set(value) = prefs.edit { putFloat("MAX_AMOUNT", value) }
    
    // Custom amount
    var customAmount: Float
        get() = prefs.getFloat("CUSTOM_AMOUNT", 0.0f)
        set(value) = prefs.edit { putFloat("CUSTOM_AMOUNT", value) }
    
    // Connection settings
    var myposApiKey: String
        get() = prefs.getString("MYPOS_API_KEY", "") ?: ""
        set(value) = prefs.edit { putString("MYPOS_API_KEY", value) }
    
    var revolutApiKey: String
        get() = prefs.getString("REVOLUT_API_KEY", "") ?: ""
        set(value) = prefs.edit { putString("REVOLUT_API_KEY", value) }
    
    // App state
    var isFirstRun: Boolean
        get() = prefs.getBoolean("FIRST_RUN", true)
        set(value) = prefs.edit { putBoolean("FIRST_RUN", value) }

    // Clear all settings
    fun resetAllSettings() {
        prefs.edit { clear() }
    }
} 