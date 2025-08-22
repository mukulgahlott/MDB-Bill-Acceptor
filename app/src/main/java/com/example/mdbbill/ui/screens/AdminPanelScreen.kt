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
import kotlinx.coroutines.launch

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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var lastSnackbarSuccess by remember { mutableStateOf(false) }

    // Local input states for validation before saving
    var minInput by remember(minimumAmount) { mutableStateOf(minimumAmount.toString()) }
    var maxInput by remember(maximumAmount) { mutableStateOf(maximumAmount.toString()) }
    // Local inputs for preferred amounts to validate on save
    var pref1Input by remember(predefinedAmount1) { mutableStateOf(predefinedAmount1.toString()) }
    var pref2Input by remember(predefinedAmount2) { mutableStateOf(predefinedAmount2.toString()) }
    var pref3Input by remember(predefinedAmount3) { mutableStateOf(predefinedAmount3.toString()) }
    
    // Handle successful test connection
    LaunchedEffect(Unit) { delay(0) }
    
    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                val bg = if (lastSnackbarSuccess) Color(0xFFD1FAE5) else Color(0xFFFECACA)
                val fg = if (lastSnackbarSuccess) Color(0xFF065F46) else Color(0xFF7F1D1D)
                Snackbar(
                    snackbarData = data,
                    containerColor = bg,
                    contentColor = fg,
                    actionContentColor = fg,
                    dismissActionContentColor = fg
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFF))
                .padding(innerPadding)
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
                    value = pref1Input,
                    onValueChange = { pref1Input = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
                LabeledCurrencyField(
                    title = "Preferred Amount 2",
                    value = pref2Input,
                    onValueChange = { pref2Input = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
                LabeledCurrencyField(
                    title = "Preferred Amount 3",
                    value = pref3Input,
                    onValueChange = { pref3Input = it }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Amount Limits Section
            SettingsSection(title = "AMOUNT LIMITS") {
                LabeledCurrencyField(
                    title = "Minimum Amount",
                    value = minInput,
                    onValueChange = { minInput = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
                LabeledCurrencyField(
                    title = "Maximum Amount",
                    value = maxInput,
                    onValueChange = { maxInput = it }
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
                onClick = {
                    val min = minInput.toFloatOrNull()
                    val max = maxInput.toFloatOrNull()
                    val p1 = pref1Input.toIntOrNull()
                    val p2 = pref2Input.toIntOrNull()
                    val p3 = pref3Input.toIntOrNull()

                    val error: String? = when {
                        min == null || max == null -> "Enter valid numeric amounts"
                        min <= 0f -> "Minimum must be greater than 0"
                        max < min -> "Maximum must be greater than or equal to minimum"
                        kotlin.math.floor(max.toDouble()) >= 1_000_000 -> "Maximum must be at most 6 digits"
                        p1 == null || p2 == null || p3 == null -> "Enter valid numeric preferred amounts"
                        p1 <= 0 || p2 <= 0 || p3 <= 0 -> "Preferred amounts must be greater than 0"
                        p1.toFloat() < min || p2.toFloat() < min || p3.toFloat() < min -> "Preferred amounts cannot be less than minimum ($min)"
                        p1.toFloat() > max || p2.toFloat() > max || p3.toFloat() > max -> "Preferred amounts cannot exceed maximum ($max)"
                        else -> null
                    }

                    if (error != null) {
                        lastSnackbarSuccess = false
                        scope.launch { snackbarHostState.showSnackbar(message = error) }
                    } else {
                        viewModel.setMinimumAmount(min!!)
                        viewModel.setMaximumAmount(max!!)
                        viewModel.setPredefinedAmount1(p1!!)
                        viewModel.setPredefinedAmount2(p2!!)
                        viewModel.setPredefinedAmount3(p3!!)
                        lastSnackbarSuccess = true
                        scope.launch { snackbarHostState.showSnackbar(message = "Settings saved") }
                    }
                },
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