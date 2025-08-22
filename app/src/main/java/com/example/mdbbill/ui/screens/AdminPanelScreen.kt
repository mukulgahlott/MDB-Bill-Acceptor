package com.example.mdbbill.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mdbbill.data.OperatingMode
import com.example.mdbbill.viewmodel.AdminViewModel
import kotlinx.coroutines.delay

@Composable
fun AdminPanelScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit,
    onNavigateToWelcome: () -> Unit = {}
) {
    val adminPin by viewModel.adminPin.collectAsStateWithLifecycle()
    var pin by remember { mutableStateOf(adminPin.toString()) }
    val operatingMode by viewModel.operatingMode.collectAsStateWithLifecycle()
    val predefinedAmount1 by viewModel.predefinedAmount1.collectAsStateWithLifecycle()
    val predefinedAmount2 by viewModel.predefinedAmount2.collectAsStateWithLifecycle()
    val predefinedAmount3 by viewModel.predefinedAmount3.collectAsStateWithLifecycle()
    val minimumAmount by viewModel.minimumAmount.collectAsStateWithLifecycle()
    val maximumAmount by viewModel.maximumAmount.collectAsStateWithLifecycle()
    var showPinDialog by remember { mutableStateOf(false) }
    
    // Handle successful test connection
    LaunchedEffect(Unit) { delay(0) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Admin Settings",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151)
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF6B7280)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            // Predefined Amounts Section
            SettingsSection(title = "PREFERRED AMOUNTS") {
                LabeledCurrencyField(
                    title = "Preferred Amount 1",
                    value = predefinedAmount1.toString(),
                    onValueChange = { viewModel.setPredefinedAmount1(it.toIntOrNull() ?: 0) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                LabeledCurrencyField(
                    title = "Preferred Amount 2",
                    value = predefinedAmount2.toString(),
                    onValueChange = { viewModel.setPredefinedAmount2(it.toIntOrNull() ?: 0) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                LabeledCurrencyField(
                    title = "Preferred Amount 3",
                    value = predefinedAmount3.toString(),
                    onValueChange = { viewModel.setPredefinedAmount3(it.toIntOrNull() ?: 0) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Amount Limits Section
            SettingsSection(title = "AMOUNT LIMITS") {
                LabeledCurrencyField(
                    title = "Minimum Amount",
                    value = minimumAmount.toString(),
                    onValueChange = { viewModel.setMinimumAmount(it.toFloatOrNull() ?: 0f) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                LabeledCurrencyField(
                    title = "Maximum Amount",
                    value = maximumAmount.toString(),
                    onValueChange = { viewModel.setMaximumAmount(it.toFloatOrNull() ?: 0f) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Operating Mode Section
            SettingsSection(title = "OPERATING MODE") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3F6FF), RoundedCornerShape(12.dp))
                        .padding(vertical = 12.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = operatingMode == OperatingMode.LIVE,
                            onCheckedChange = { if (it) viewModel.setMode(OperatingMode.LIVE) }
                        )
                        Text(
                            text = "Live",
                            style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF374151))
                        )
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = operatingMode == OperatingMode.TRAINING,
                            onCheckedChange = { if (it) viewModel.setMode(OperatingMode.TRAINING) }
                        )
                        Text(
                            text = "Training",
                            style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF9CA3AF))
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { /* values are persisted on change */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6C85F7),
                    contentColor = Color.White
                )
            ) {
                Text("Save Changes", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = { showPinDialog = true },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Change PIN", color = Color(0xFF6C85F7))
            }

            if (showPinDialog) {
                AlertDialog(
                    onDismissRequest = { showPinDialog = false },
                    title = { Text("Change PIN") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = pin,
                                onValueChange = {
                                    if (it.length <= 4 && it.all { c -> c.isDigit() }) pin = it
                                },
                                label = { Text("4-Digit PIN") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.setAdminPin(pin)
                            showPinDialog = false
                        }) { Text("Save") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPinDialog = false }) { Text("Cancel") }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
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
            containerColor = Color(0xFF5B8AEE).copy(0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6B7280)
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun LabeledCurrencyField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            leadingIcon = { Text("$") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}