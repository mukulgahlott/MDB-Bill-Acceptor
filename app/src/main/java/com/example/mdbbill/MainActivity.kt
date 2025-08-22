package com.example.mdbbill

import android.content.Intent
import android.os.Bundle
import android.app.ActivityManager
import android.content.Context
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.mdbbill.mdb.MdbController
import com.example.mdbbill.navigation.NavGraph
import com.example.mdbbill.payment.MyPosGateway
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize MDB controller
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

    private fun isLockTaskPermitted(): Boolean {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return am.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE || am.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_PINNED
    }
}