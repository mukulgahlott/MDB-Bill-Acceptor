package com.example.mdbbill.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(
    onNavigateToPassword: () -> Unit,
    onNavigateToAmountSelection: () -> Unit
) {
    var longPressCount by remember { mutableIntStateOf(0) }
    var showHiddenMessage by remember { mutableStateOf(false) }
    var isLongPressing by remember { mutableStateOf(false) }
    var longPressProgress by remember { mutableFloatStateOf(0f) }
    
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1976D2),
            Color(0xFF1565C0),
            Color(0xFF0D47A1)
        )
    )
    
    LaunchedEffect(longPressCount) {
        if (longPressCount >= 5) {
            showHiddenMessage = true
            delay(2000)
            showHiddenMessage = false
            longPressCount = 0
            onNavigateToPassword()
        }
    }
    
    LaunchedEffect(isLongPressing) {
        if (isLongPressing) {
            longPressProgress = 0f
            while (isLongPressing && longPressProgress < 1f) {
                delay(10)
                longPressProgress += 0.01f
            }
            if (longPressProgress >= 1f) {
                onNavigateToAmountSelection()
            }
            isLongPressing = false
            longPressProgress = 0f
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo/Title
            Card(
                modifier = Modifier
                    .size(120.dp)
                    .clickable { longPressCount++ },
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "MDB Bill",
                        modifier = Modifier.size(60.dp),
                        tint = Color(0xFF1976D2)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // App Title
            Text(
                text = "MDB Bill",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Payment Terminal",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White.copy(alpha = 0.8f)
                ),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Get Started Button with Long Press
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isLongPressing = true
                                tryAwaitRelease()
                                isLongPressing = false
                            }
                        )
                    }
            ) {
                // Progress ring
                if (isLongPressing) {
                    CircularProgressIndicator(
                    progress = { longPressProgress },
                    modifier = Modifier
                                                .size(200.dp)
                                                .align(Alignment.Center),
                    color = Color.White,
                    strokeWidth = 8.dp,
                    trackColor = Color.White.copy(alpha = 0.3f),
                    strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
                    )
                }
                
                // Main button
                Column (modifier = Modifier
                        .size(180.dp)
                        .align(Alignment.Center).background(Color.White, shape = RoundedCornerShape(200.dp)),
                    ) {
                    Column(modifier = Modifier, verticalArrangement = Arrangement.Center) {
                        Spacer(modifier = Modifier.height(65.dp))
                        Text(
                            modifier = Modifier.fillMaxSize(),
                            fontSize = 26.sp,
                            text = "PUSH TO\nSTART",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Instructions
         //   Text(
           //     text = "Hold the button for 1 second to begin",
             //   style = MaterialTheme.typography.bodyMedium.copy(
             //       color = Color.White.copy(alpha = 0.8f)
               // ),
              //  textAlign = TextAlign.Center
           // )
        }
        
        // Hidden message for admin access
        if (showHiddenMessage) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF5722)
                )
            ) {
                Text(
                    text = "Admin Access Granted",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
} 