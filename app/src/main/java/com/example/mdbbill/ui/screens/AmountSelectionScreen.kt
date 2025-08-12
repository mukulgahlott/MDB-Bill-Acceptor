package com.example.mdbbill.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mdbbill.viewmodel.PaymentResult
import com.example.mdbbill.viewmodel.PaymentViewModel

@Composable
fun AmountSelectionScreen(
    viewModel: PaymentViewModel,
    onBack: () -> Unit
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1976D2),
            Color(0xFF1565C0),
            Color(0xFF0D47A1)
        )
    )
    
    val selectedAmount by viewModel.selectedAmount.collectAsStateWithLifecycle()
    val customAmount by viewModel.customAmount.collectAsStateWithLifecycle()
    val isPaymentProcessing by viewModel.isPaymentProcessing.collectAsStateWithLifecycle()
    val paymentResult by viewModel.paymentResult.collectAsStateWithLifecycle()
    
    LaunchedEffect(paymentResult) {
        paymentResult?.let {
            if (it is PaymentResult.Success) {
                // Auto-clear success result after 3 seconds
                kotlinx.coroutines.delay(3000)
                viewModel.clearPaymentResult()
                viewModel.resetAmount()
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
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
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Text(
                text = "Select Amount",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Selected amount display
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Selected Amount",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "€${String.format("%.2f", selectedAmount)}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Predefined amounts
            Text(
                text = "Quick Select",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Amount buttons grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AmountButton(
                    amount = 5.0f,
                    isSelected = selectedAmount == 5.0f,
                    onClick = { viewModel.setSelectedAmount(5.0f) },
                    modifier = Modifier.weight(1f)
                )
                AmountButton(
                    amount = 10.0f,
                    isSelected = selectedAmount == 10.0f,
                    onClick = { viewModel.setSelectedAmount(10.0f) },
                    modifier = Modifier.weight(1f)
                )
                AmountButton(
                    amount = 20.0f,
                    isSelected = selectedAmount == 20.0f,
                    onClick = { viewModel.setSelectedAmount(20.0f) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Custom amount section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Custom Amount",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customAmount,
                        onValueChange = { 
                            viewModel.setCustomAmount(it)
                            viewModel.setSelectedAmount(0f) // Clear predefined selection
                        },
                        label = { Text("Enter amount", color = Color.White.copy(alpha = 0.7f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedLabelColor = Color(0xFF1976D2),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                            cursorColor = Color(0xFF1976D2),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        singleLine = true,
                        prefix = { Text("€", color = Color.White.copy(alpha = 0.7f)) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Payment button
            Button(
                onClick = { viewModel.processPayment() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1976D2)
                ),
                enabled = !isPaymentProcessing && (selectedAmount > 0 || customAmount.isNotEmpty())
            ) {
                if (isPaymentProcessing) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF1976D2),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Opening myPOS...",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Process Payment",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
            
            // Payment result
            paymentResult?.let { result ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (result) {
                            is PaymentResult.Success -> Color(0xFF2E7D32)
                            is PaymentResult.Error -> Color(0xFFD32F2F)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when (result) {
                                is PaymentResult.Success -> "myPOS Payment Successful!"
                                is PaymentResult.Error -> "myPOS Payment Failed"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (result) {
                                is PaymentResult.Success -> "€${String.format("%.2f", result.amount)}"
                                is PaymentResult.Error -> result.message
                            },
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White
                            ),
                            textAlign = TextAlign.Center
                        )
                        if (result is PaymentResult.Success) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Receipt printed & MDB communication completed",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White.copy(alpha = 0.8f)
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
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