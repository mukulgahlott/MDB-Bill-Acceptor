package com.example.mdbbill.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
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
import com.example.mdbbill.data.OperatingMode
import com.example.mdbbill.data.PaymentProcessor
import com.example.mdbbill.viewmodel.AdminViewModel
import kotlinx.coroutines.delay

@Composable
fun AdminPanelScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit,
    onNavigateToWelcome: () -> Unit = {}
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1976D2),
            Color(0xFF1565C0),
            Color(0xFF0D47A1)
        )
    )

    val adminPin by viewModel.adminPin.collectAsStateWithLifecycle()
    var pin by remember { mutableStateOf(adminPin.toString()) }
    val operatingMode by viewModel.operatingMode.collectAsStateWithLifecycle()
    val paymentProcessor by viewModel.paymentProcessor.collectAsStateWithLifecycle()
    val predefinedAmount1 by viewModel.predefinedAmount1.collectAsStateWithLifecycle()
    val predefinedAmount2 by viewModel.predefinedAmount2.collectAsStateWithLifecycle()
    val predefinedAmount3 by viewModel.predefinedAmount3.collectAsStateWithLifecycle()
    val minimumAmount by viewModel.minimumAmount.collectAsStateWithLifecycle()
    val maximumAmount by viewModel.maximumAmount.collectAsStateWithLifecycle()
    val myposApiKey by viewModel.myposApiKey.collectAsStateWithLifecycle()
    val revolutApiKey by viewModel.revolutApiKey.collectAsStateWithLifecycle()
    val isTestConnectionLoading by viewModel.isTestConnectionLoading.collectAsStateWithLifecycle()
    val testConnectionResult by viewModel.testConnectionResult.collectAsStateWithLifecycle()
    
    // Handle successful test connection
    LaunchedEffect(testConnectionResult) {
        if (testConnectionResult?.contains("successful") == true) {
            delay(2000) // Show success message for 2 seconds
            onNavigateToWelcome()
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Admin Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Predefined Amounts Section
            SettingsSection(title = "Predefined Amounts") {
                AmountInputField(
                    label = "Amount 1",
                    value = predefinedAmount1.toString(),
                    onValueChange = { viewModel.setPredefinedAmount1(it.toFloatOrNull() ?: 0f) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                AmountInputField(
                    label = "Amount 2",
                    value = predefinedAmount2.toString(),
                    onValueChange = { viewModel.setPredefinedAmount2(it.toFloatOrNull() ?: 0f) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                AmountInputField(
                    label = "Amount 3",
                    value = predefinedAmount3.toString(),
                    onValueChange = { viewModel.setPredefinedAmount3(it.toFloatOrNull() ?: 0f) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Amount Limits Section
            SettingsSection(title = "Amount Limits") {
                AmountInputField(
                    label = "Minimum Amount",
                    value = minimumAmount.toString(),
                    onValueChange = { viewModel.setMinimumAmount(it.toFloatOrNull() ?: 0f) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                AmountInputField(
                    label = "Maximum Amount",
                    value = maximumAmount.toString(),
                    onValueChange = { viewModel.setMaximumAmount(it.toFloatOrNull() ?: 0f) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Operating Mode Section
            SettingsSection(title = "Operating Mode") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = operatingMode == OperatingMode.LIVE,
                        onClick = { viewModel.setMode(OperatingMode.LIVE) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.White,
                            unselectedColor = Color.White.copy(alpha = 0.6f)
                        )
                    )
                    Text(
                        text = "LIVE",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Spacer(modifier = Modifier.width(32.dp))
                    RadioButton(
                        selected = operatingMode == OperatingMode.TRAINING,
                        onClick = { viewModel.setMode(OperatingMode.TRAINING) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.White,
                            unselectedColor = Color.White.copy(alpha = 0.6f)
                        )
                    )
                    Text(
                        text = "TRAINING",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Payment Processor Section
            SettingsSection(title = "Payment Processor") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.setProcessor(PaymentProcessor.MYPOS) },
                        modifier = Modifier.weight(1f),
                        colors = if (paymentProcessor == PaymentProcessor.MYPOS) {
                            ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1976D2))
                        } else {
                            ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            )
                        },
                        border = if (paymentProcessor != PaymentProcessor.MYPOS) {
                            ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    listOf(Color.White, Color.White.copy(alpha = 0.6f))
                                )
                            )
                        } else null
                    ) {
                        Text("myPOS", fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = { viewModel.setProcessor(PaymentProcessor.REVOLUT) },
                        modifier = Modifier.weight(1f),
                        colors = if (paymentProcessor == PaymentProcessor.REVOLUT) {
                            ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1976D2))
                        } else {
                            ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            )
                        },
                        border = if (paymentProcessor != PaymentProcessor.REVOLUT) {
                            ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    listOf(Color.White, Color.White.copy(alpha = 0.6f))
                                )
                            )
                        } else null
                    ) {
                        Text("Revolut", fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // API Keys Section
            SettingsSection(title = "API Keys") {
                OutlinedTextField(
                    value = myposApiKey,
                    onValueChange = { viewModel.setMyposApiKey(it) },
                    label = { Text("myPOS API Key", color = Color.White.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = revolutApiKey,
                    onValueChange = { viewModel.setRevolutApiKey(it) },
                    label = { Text("Revolut API Key", color = Color.White.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Admin PIN Section
            SettingsSection(title = "Admin PIN") {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { 
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            pin = it
                        }
                    },
                    label = { Text("4-Digit PIN", color = Color.White.copy(alpha = 0.7f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.setAdminPin(pin) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1976D2)
                    ),
                    enabled = !isTestConnectionLoading
                ) {
                    if (isTestConnectionLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color(0xFF1976D2),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Apply", fontWeight = FontWeight.Bold)
                    }
                }
                
                Button(
                    onClick = { viewModel.resetApp() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White
                    )
                ) {
                    Text("Reset App", fontWeight = FontWeight.Bold)
                }
            }
            
            // Test connection result
            testConnectionResult?.let { result ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.contains("successful")) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                    )
                ) {
                    Text(
                        text = result,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
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
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun AmountInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White.copy(alpha = 0.7f)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
            cursorColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        ),
        singleLine = true
    )
} 