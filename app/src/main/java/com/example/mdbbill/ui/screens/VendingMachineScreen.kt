package com.example.mdbbill.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mdbbill.MainActivity
import com.example.mdbbill.peripheral.Event

data class VendingProduct(
    val id: Int,
    val name: String,
    val price: Int, // Price in cents
    val available: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendingMachineScreen() {
    val context = LocalContext.current
    val mainActivity = context as MainActivity
    
    var selectedProduct by remember { mutableStateOf<VendingProduct?>(null) }
    var vendingStatus by remember { mutableStateOf("Ready") }
    var cashlessReady by remember { mutableStateOf(false) }
    var billValidatorReady by remember { mutableStateOf(false) }
    var coinChangerReady by remember { mutableStateOf(false) }

    // Sample products
    val products = listOf(
        VendingProduct(1, "Coca Cola", 150), // $1.50
        VendingProduct(2, "Pepsi", 150),     // $1.50
        VendingProduct(3, "Water", 100),     // $1.00
        VendingProduct(4, "Chips", 125),     // $1.25
        VendingProduct(5, "Candy", 75)       // $0.75
    )

    // Handle vending machine events
    LaunchedEffect(Unit) {
        // In a real implementation, you would observe the events from the MainActivity
        // For now, we'll simulate the device states
        cashlessReady = true
        billValidatorReady = true
        coinChangerReady = true
        vendingStatus = "All devices ready"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Vending Machine",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Device Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (cashlessReady && billValidatorReady && coinChangerReady) 
                    MaterialTheme.colorScheme.primaryContainer 
                else MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Device Status",
                    fontWeight = FontWeight.Bold
                )
                Text("Cashless Device: ${if (cashlessReady) "Ready" else "Not Ready"}")
                Text("Bill Validator: ${if (billValidatorReady) "Ready" else "Not Ready"}")
                Text("Coin Changer: ${if (coinChangerReady) "Ready" else "Not Ready"}")
                Text(
                    text = vendingStatus,
                    fontWeight = FontWeight.Bold,
                    color = if (cashlessReady && billValidatorReady && coinChangerReady) 
                        MaterialTheme.colorScheme.primary 
                    else MaterialTheme.colorScheme.error
                )
            }
        }

        // Product Selection
        Text(
            text = "Select Product",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products) { product ->
                ProductCard(
                    product = product,
                    isSelected = selectedProduct?.id == product.id,
                    onProductSelected = { selectedProduct = product }
                )
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    selectedProduct?.let { product ->
                        mainActivity.sendPaymentRequest(product.id, product.price)
                        vendingStatus = "Payment request sent for ${product.name}"
                    }
                },
                enabled = selectedProduct != null && cashlessReady,
                modifier = Modifier.weight(1f)
            ) {
                Text("Pay with Card")
            }

            Button(
                onClick = {
                    selectedProduct?.let { product ->
                        mainActivity.sendPaymentRequest(product.id, product.price)
                        vendingStatus = "Payment request sent for ${product.name}"
                    }
                },
                enabled = selectedProduct != null && billValidatorReady,
                modifier = Modifier.weight(1f)
            ) {
                Text("Pay with Cash")
            }
        }

        Button(
            onClick = {
                selectedProduct?.let { product ->
                    mainActivity.cancelVending(product.id)
                    vendingStatus = "Vending cancelled"
                }
            },
            enabled = selectedProduct != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }

        // Instructions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Instructions",
                    fontWeight = FontWeight.Bold
                )
                Text("1. Select a product from the list above")
                Text("2. Choose payment method (Card or Cash)")
                Text("3. Complete payment on the device")
                Text("4. Product will be dispensed automatically")
                Text("5. Use Cancel button to abort transaction")
            }
        }
    }
}

@Composable
fun ProductCard(
    product: VendingProduct,
    isSelected: Boolean,
    onProductSelected: () -> Unit
) {
    Card(
        onClick = onProductSelected,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Product #${product.id}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "$${product.price / 100.0}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
