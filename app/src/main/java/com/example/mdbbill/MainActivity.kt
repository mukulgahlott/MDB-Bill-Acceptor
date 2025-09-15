package com.example.mdbbill

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.hardware.mdbMaster.MdbMaster
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.mdbbill.mdb.MdbController
import com.example.mdbbill.mdb.VendingMachineController
import com.example.mdbbill.navigation.NavGraph
import com.example.mdbbill.payment.MyPosGateway
import com.example.mdbbill.peripheral.Event
import com.example.mdbbill.ui.theme.MdbBillTheme
import com.example.mdbbill.viewmodel.AdminViewModel
import com.example.mdbbill.viewmodel.PaymentViewModel

class MainActivity : ComponentActivity() {

    // Activity result launcher for myPOS payment
    private val paymentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        MyPosGateway.handlePaymentResult(
            MyPosGateway.PAYMENT_REQUEST_CODE,
            result.resultCode,
            result.data
        )
    }

    // Vending Machine Controller
    private lateinit var vmController: VendingMachineController
    private lateinit var eventHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize event handler for MDB events
        eventHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                handleVendingEvents(msg.what)
            }
        }

        // Initialize Vending Machine Controller
        vmController = VendingMachineController.getInstance()
        if (vmController.initialize(eventHandler)) {
            vmController.start()
            Log.d("MainActivity", "Vending Machine Controller initialized successfully")
        } else {
            Log.e("MainActivity", "Failed to initialize Vending Machine Controller")
        }

        // Initialize MDB controller (legacy)
        val mdbController = MdbController(this)
        mdbController.initializeMdb()

        setContent {
            MdbBillTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val adminViewModel: AdminViewModel = viewModel()
                    val paymentViewModel: PaymentViewModel = viewModel()

                    // Observe payment state and launch myPOS when needed
                    LaunchedEffect(Unit) {
                        paymentViewModel.shouldLaunchPayment.collect { shouldLaunch ->
                            if (shouldLaunch) {
                                paymentViewModel.launchMyPosPayment(this@MainActivity)
                            }
                        }
                    }

                    NavGraph(
                        navController = navController,
                        adminViewModel = adminViewModel,
                        paymentViewModel = paymentViewModel
                    )
                }
            }
        }
    }

    // Handle activity result for myPOS payment (legacy method for compatibility)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        MyPosGateway.handlePaymentResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        super.onStart()
        if (isLockTaskPermitted()) {
            startLockTask()
        }
    }

    override fun onStop() {
        super.onStop()
        stopLockTask()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up Vending Machine Controller
        vmController.destroy()
    }

    private fun isLockTaskPermitted(): Boolean {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return am.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE || am.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_PINNED
    }

    /**
     * Handle vending machine events from MDB devices
     */
    private fun handleVendingEvents(event: Int) {
        when (event) {
            Event.EV_CASHLESS_DETECTED -> {
                Log.d("MainActivity", "Cashless device detected and ready")
                // Update UI to show cashless device is ready
            }
            Event.EV_CASHLESS_BEGIN_SESSION -> {
                Log.d("MainActivity", "Payment session started - Please select a product")
                // Update UI to allow product selection
            }
            Event.EV_CASHLESS_VEND_APPROVED -> {
                Log.d("MainActivity", "Product approved for dispensing")
                // Trigger product dispensing mechanism
                // Here you would integrate with your product dispensing system
            }
            Event.EV_CASHLESS_VEND_DENIED -> {
                Log.d("MainActivity", "Product payment denied")
                // Update UI to show payment denied
            }
            Event.EV_CASHLESS_END_SESSION -> {
                Log.d("MainActivity", "Payment session ended")
                // Update UI to show session ended
            }
            Event.EV_CASHLESS_SESSION_CANCEL -> {
                Log.d("MainActivity", "Payment session cancelled")
                // Update UI to show session cancelled
            }
            Event.EV_BILL_VALIDATOR_DETECTED -> {
                Log.d("MainActivity", "Bill validator detected and ready")
                // Update UI to show bill validator is ready
            }
            Event.EV_BILL_VALIDATOR_INSERT_BILL -> {
                Log.d("MainActivity", "Bill inserted")
                // Update UI to show bill inserted
            }
            Event.EV_BILL_VALIDATOR_ENTER_SALE_MODE -> {
                Log.d("MainActivity", "Bill validator entered sale mode")
                // Update UI to show bill validator is ready for sales
            }
            Event.EV_COIN_CHANGER_DETECTED -> {
                Log.d("MainActivity", "Coin changer detected and ready")
                // Update UI to show coin changer is ready
            }
            Event.EV_COIN_CHANGER_ACTIVITY_TYPE_REPORT -> {
                Log.d("MainActivity", "Coin changer activity reported")
                // Handle coin changer activity
            }
            else -> {
                Log.d("MainActivity", "Unhandled vending event: $event")
            }
        }
    }

    /**
     * Send payment request to vending machine
     * This method can be called from UI components to initiate payment
     */
    fun sendPaymentRequest(itemNumber: Int, itemPrice: Int) {
        vmController.executeItemSelected(itemNumber, itemPrice)
        Log.d("MainActivity", "Payment request sent for Product $itemNumber ($${itemPrice / 100.0})")
    }

    /**
     * Cancel current vending operation
     */
    fun cancelVending(itemNumber: Int) {
        vmController.executeCancelVend(itemNumber)
        Log.d("MainActivity", "Vending cancelled for Product $itemNumber")
    }
}