package com.example.mdbbill.ui.screens
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mdbbill.R
import com.example.mdbbill.viewmodel.AdminViewModel
import com.example.mdbbill.viewmodel.PaymentResult
import com.example.mdbbill.viewmodel.PaymentViewModel
 

@Composable
fun AmountSelectionScreen(
    viewModel: PaymentViewModel,
    adminViewModel: AdminViewModel = viewModel(),
    onBack: () -> Unit
) {
    val selectedAmount by viewModel.selectedAmount.collectAsStateWithLifecycle()
    val customAmount by viewModel.customAmount.collectAsStateWithLifecycle()
    val isPaymentProcessing by viewModel.isPaymentProcessing.collectAsStateWithLifecycle()
    val paymentResult by viewModel.paymentResult.collectAsStateWithLifecycle()
    val predefinedAmount2 by adminViewModel.predefinedAmount2.collectAsStateWithLifecycle()
    val predefinedAmount1 by adminViewModel.predefinedAmount1.collectAsStateWithLifecycle()
    val predefinedAmount3 by adminViewModel.predefinedAmount3.collectAsStateWithLifecycle()

    // Snackbars below handle result visibility and clearing

    val background = Color(0xFFF5F7FB)
    val primary = Color(0xFF5B86E5)
    val textPrimary = Color(0xFF5B6573)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        // Back button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = textPrimary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top: 40%
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 26.dp, horizontal = 16.dp)
                    .weight(0.4f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Amount",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        val displayText =
                            customAmount.ifEmpty { String.format("%.0f", selectedAmount) }
                        Text(
                            text = "$$displayText",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Quick Select",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = textPrimary
                    ),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickAmountButton("$$predefinedAmount1", selectedAmount == predefinedAmount1.toFloat(), modifier = Modifier.weight(1f)) {
                        viewModel.setSelectedAmount(predefinedAmount1.toFloat())
                        viewModel.setCustomAmount("")
                    }
                    QuickAmountButton("$$predefinedAmount2", selectedAmount == predefinedAmount2.toFloat(), modifier = Modifier.weight(1f)) {
                        viewModel.setSelectedAmount(predefinedAmount2.toFloat())
                        viewModel.setCustomAmount("")
                    }
                    QuickAmountButton("$$predefinedAmount3", selectedAmount == predefinedAmount3.toFloat(), modifier = Modifier.weight(1f)) {
                        viewModel.setSelectedAmount(predefinedAmount3.toFloat())
                        viewModel.setCustomAmount("")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom: 60% keypad
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F2F6))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left grid (3 columns x 4 rows)
                    Column(
                        modifier = Modifier.weight(3f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            KeypadButton("1", modifier = Modifier.weight(1f)) { onDigitPressed(viewModel, customAmount, "1") }
                            KeypadButton("2", modifier = Modifier.weight(1f)) { onDigitPressed(viewModel, customAmount, "2") }
                            KeypadButton("3", modifier = Modifier.weight(1f)) { onDigitPressed(viewModel, customAmount, "3") }
                        }
                        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            KeypadButton("4", modifier = Modifier.weight(1f)) { onDigitPressed(viewModel, customAmount, "4") }
                            KeypadButton("5", modifier = Modifier.weight(1f)) { onDigitPressed(viewModel, customAmount, "5") }
                            KeypadButton("6", modifier = Modifier.weight(1f)) { onDigitPressed(viewModel, customAmount, "6") }
                        }
                        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            KeypadButton("7", modifier = Modifier.weight(1f)) { onDigitPressed(viewModel, customAmount, "7") }
                            KeypadButton("8", modifier = Modifier.weight(1f)) { onDigitPressed(viewModel, customAmount, "8") }
                            KeypadButton("9", modifier = Modifier.weight(1f)) { onDigitPressed(viewModel, customAmount, "9") }
                        }
                        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            KeypadButton("00", modifier = Modifier.weight(1f)) { onDigitPressed(viewModel, customAmount, "00") }
                            KeypadButton("0", modifier = Modifier.weight(1f)) { onDigitPressed(viewModel, customAmount, "0") }
                            KeypadButton("00", modifier = Modifier.weight(1f)) { onDigitPressed(viewModel, customAmount, "00") }
                        }
                    }

                    // Right column: backspace on top, enter below spanning 3 rows
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                if (customAmount.isNotEmpty()) {
                                    viewModel.setCustomAmount(customAmount.dropLast(1))
                                    viewModel.setSelectedAmount(0f)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.White)
                        ) {
                            Image(
                                modifier = Modifier.padding(8.dp),
                                painter = painterResource(id = R.drawable.back_space),
                                contentDescription = "Backspace"
                            )
                        }

                        Button(
                            onClick = {
                                if (customAmount.isNotEmpty()) {
                                    customAmount.toFloatOrNull()?.let { viewModel.setSelectedAmount(it) }
                                }
                                if (!isPaymentProcessing && (viewModel.selectedAmount.value > 0f)) {
                                    viewModel.processPayment()
                                }
                            },
                            enabled = !isPaymentProcessing && ((customAmount.isNotEmpty() && customAmount.toFloatOrNull() != null) || selectedAmount > 0f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(3f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primary, disabledContainerColor = primary, contentColor = Color.White)
                        ) {
                            Image(
                                modifier = Modifier.padding(8.dp),
                                painter = painterResource(id = R.drawable.enter),
                                contentDescription = "Enter"
                            )
                        }
                    }
                }
            }
        }

        // Snackbar overlay
        var snackbarVisible by remember { mutableStateOf(false) }
        var snackbarText by remember { mutableStateOf("") }
        var snackbarSuccess by remember { mutableStateOf(true) }

        LaunchedEffect(paymentResult) {
            when (val r = paymentResult) {
                is PaymentResult.Success -> {
                    snackbarText = "Payment successful: $${String.format("%.2f", r.amount)}"
                    snackbarSuccess = true
                    snackbarVisible = true
                    // Reset amount after success
                    viewModel.resetAmount()
                }
                is PaymentResult.Error -> {
                    snackbarText = r.message
                    snackbarSuccess = false
                    snackbarVisible = true
                }
                null -> {}
            }
        }

        LaunchedEffect(snackbarVisible) {
            if (snackbarVisible) {
                kotlinx.coroutines.delay(4000)
                snackbarVisible = false
                viewModel.clearPaymentResult()
            }
        }

        DismissibleSnackbar(
            visible = snackbarVisible,
            message = snackbarText,
            isSuccess = snackbarSuccess,
            onDismiss = {
                snackbarVisible = false
                viewModel.clearPaymentResult()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
        )
    }
}

@Composable
private fun AmountButton(
    amount: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = if (isSelected) {
            ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF1976D2)
            )
        } else {
            ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            )
        },
        border = if (!isSelected) {
            ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(Color.White, Color.White.copy(alpha = 0.6f))
                )
            )
        } else null
    ) {
        Text(
            text = "€${String.format("%.0f", amount)}",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun QuickAmountButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val primary = Color(0xFF5B86E5)

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(75.dp)
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                val dashLength = 10.dp.toPx()
                val gapLength = 6.dp.toPx()

                // Create a dashed stroke style
                val stroke = Stroke(
                    width = strokeWidth,
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(dashLength, gapLength), 0f
                    )
                )
                val shape = RoundedCornerShape(8.dp)
                val outline = shape.createOutline(size, layoutDirection, this)

                drawOutline(
                    outline = outline,
                    color = primary,
                    style = stroke
                )
            },
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) Color(0xFFE8F0FF) else Color.White,
            contentColor = primary
        ),
        border = null // remove default border
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun KeypadButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxHeight(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF5B6573))
    ) {
        Text(text = label, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
    }
}

private fun onDigitPressed(viewModel: PaymentViewModel, current: String, token: String) {
    val newValue = (current + token).take(9)
    viewModel.setCustomAmount(newValue.trimStart('0').ifEmpty { "0" })
    viewModel.setSelectedAmount(0f)
}

@Composable
private fun DismissibleSnackbar(
    visible: Boolean,
    message: String,
    isSuccess: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSuccess) Color(0xFF2E7D32) else Color(0xFFD32F2F)

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            color = bg,
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 2.dp,
            shadowElevation = 4.dp
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                textAlign = TextAlign.Center
            )
        }
    }
}