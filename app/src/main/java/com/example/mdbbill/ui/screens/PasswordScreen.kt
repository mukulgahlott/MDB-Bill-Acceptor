package com.example.mdbbill.ui.screens

import android.R.attr.onClick
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mdbbill.R
import com.example.mdbbill.viewmodel.AdminViewModel
import kotlinx.coroutines.delay

@Composable
fun PasswordScreen(
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: AdminViewModel,
) {
    var pin by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val adminPin by viewModel.adminPin.collectAsStateWithLifecycle()

    fun appendDigits(digits: String) {
        for (ch in digits) {
            if (pin.length < 4 && ch.isDigit()) {
                pin += ch
            }
        }
        if (showError) {
            showError = false
            errorMessage = ""
        }
    }

    LaunchedEffect(showError) {
        if (showError) {
            delay(3000)
            showError = false
            errorMessage = ""
        }
    }

    val digitButtonColor = Color(0xFFF1F3F6)
    val digitTextColor = Color(0xFF263238)
    val actionBlue = Color(0xFF5C78FF)
    val keypadBackground = Color(0xFFF4F6F8)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top-left nav back (optional)
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF607D8B)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 36.dp)
                .padding(horizontal = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Lock image inside a circle
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(color = actionBlue.copy(alpha = 0.9f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.lock),
                        contentDescription = "Lock",
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Enter PIN to continue",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF455A64)
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // PIN dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val filled = index < pin.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(
                                    color = if (filled) actionBlue else Color.Transparent,
                                    shape = CircleShape
                                )
                                .then(
                                    if (!filled) Modifier.border(
                                        width = 2.dp,
                                        color = Color(0xFFB0BEC5),
                                        shape = CircleShape
                                    ) else Modifier
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = if (showError) {
                        errorMessage
                    } else "",
                    color = Color(0xFFD32F2F),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Keypad
            Surface(

                color = keypadBackground,
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left digits grid (3 columns)
                    Column(
                        modifier = Modifier.weight(3f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DigitButton("1", modifier = Modifier.weight(1f)) { appendDigits("1") }
                            DigitButton("2", modifier = Modifier.weight(1f)) { appendDigits("2") }
                            DigitButton("3", modifier = Modifier.weight(1f)) { appendDigits("3") }
                        }
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DigitButton("4", modifier = Modifier.weight(1f)) { appendDigits("4") }
                            DigitButton("5", modifier = Modifier.weight(1f)) { appendDigits("5") }
                            DigitButton("6", modifier = Modifier.weight(1f)) { appendDigits("6") }
                        }
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DigitButton("7", modifier = Modifier.weight(1f)) { appendDigits("7") }
                            DigitButton("8", modifier = Modifier.weight(1f)) { appendDigits("8") }
                            DigitButton("9", modifier = Modifier.weight(1f)) { appendDigits("9") }
                        }
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DigitButton("00", modifier = Modifier.weight(1f)) { appendDigits("00") }
                            DigitButton("0", modifier = Modifier.weight(1f)) { appendDigits("0") }
                            DigitButton(
                                "00",
                                modifier = Modifier.weight(1f)
                            ) { appendDigits("00") }
                        }
                    }

                    // Right actions: backspace (top), enter (tall)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionButton(
                            modifier = Modifier.weight(1f),
                            containerColor = digitButtonColor,
                            contentColor = digitTextColor,
                            onClick = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
                            content = {
                                Image(
                                    painter = painterResource(R.drawable.back_space),
                                    modifier = Modifier.padding(10.dp),
                                    contentDescription = "Delete"
                                )
                            }
                        )

                        ActionButton(
                            modifier = Modifier.weight(3f),
                            containerColor = actionBlue,
                            contentColor = Color.White,
                            onClick = {
                                if (pin.length == 4) {
                                    if (pin == adminPin) {
                                        onLoginSuccess()
                                    } else {
                                        showError = true
                                        errorMessage = "Incorrect PIN. Please try again."
                                        pin = ""
                                    }
                                }
                            },
                            content = {
                                Image(
                                    painter = painterResource(R.drawable.enter),
                                    modifier = Modifier.padding(10.dp),
                                    contentDescription = "enter"
                                )
                            })
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DigitButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFFFFF),
            contentColor = Color(0xFF263238)
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color,
    content: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        content()
    }
}