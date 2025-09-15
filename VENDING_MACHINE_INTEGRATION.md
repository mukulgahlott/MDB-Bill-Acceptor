# Vending Machine Integration Guide

## CM30 Hardware Library - MDB Protocol Implementation

### Overview
This document provides a complete guide for implementing vending machine connection and payment functionality using the CM30 hardware library and MDB (Multi-Drop Bus) protocol. The implementation is now in Kotlin and follows modern Android development practices.

---

## 1. Core Architecture

### 1.1 Main Components
- **VendingMachineController**: Central controller managing all vending operations
- **MdbMaster**: Hardware interface for MDB communication (from CM30 library)
- **Payment Devices**: CashLessDevice, BillValidator, CoinChanger
- **UI Layer**: Compose screens for user interaction

### 1.2 Key Dependencies
```kotlin
// Already added to your build.gradle
implementation(files("libs/CM30-HardwareLibrary-1.0.9.aar"))
```

---

## 2. Implementation Details

### 2.1 VendingMachineController (Singleton Pattern)
The `VendingMachineController` is implemented as a singleton that manages all MDB device communication:

```kotlin
// Get instance
val vmController = VendingMachineController.getInstance()

// Initialize with event handler
val success = vmController.initialize(eventHandler)
if (success) {
    vmController.start() // Start the polling thread
}
```

**Key Features:**
- Singleton pattern for global access
- Continuous polling of MDB devices (100ms intervals)
- Thread-safe operations with synchronized blocks
- Automatic device initialization and management

### 2.2 Payment Device Classes

#### CashLessDevice
Handles cashless payment operations (credit cards, mobile payments, etc.):

```kotlin
// Send payment request
vmController.executeItemSelected(itemNumber, itemPrice)

// Cancel payment
vmController.executeCancelVend(itemNumber)
```

**MDB Commands Supported:**
- VEND REQUEST: Send payment request with item number and price
- VEND SUCCESS: Confirm successful product dispensing
- VEND FAILURE: Report dispensing failure
- SESSION COMPLETE: End payment session

#### BillValidator
Manages cash bill acceptance and validation:

**Events Handled:**
- `EV_BILL_VALIDATOR_DETECTED`: Device ready
- `EV_BILL_VALIDATOR_INSERT_BILL`: Bill inserted
- `EV_BILL_VALIDATOR_ENTER_SALE_MODE`: Ready for sales

#### CoinChanger
Handles coin acceptance and change dispensing:

**Events Handled:**
- `EV_COIN_CHANGER_DETECTED`: Device ready
- `EV_COIN_CHANGER_ACTIVITY_TYPE_REPORT`: Coin activity

---

## 3. Integration in MainActivity

### 3.1 Initialization
```kotlin
class MainActivity : ComponentActivity() {
    private lateinit var vmController: VendingMachineController
    private lateinit var eventHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize event handler
        eventHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                handleVendingEvents(msg.what)
            }
        }

        // Initialize VMC
        vmController = VendingMachineController.getInstance()
        if (vmController.initialize(eventHandler)) {
            vmController.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        vmController.destroy()
    }
}
```

### 3.2 Event Handling
```kotlin
private fun handleVendingEvents(event: Int) {
    when (event) {
        Event.EV_CASHLESS_DETECTED -> {
            // Cashless device ready
        }
        Event.EV_CASHLESS_VEND_APPROVED -> {
            // Payment approved - dispense product
        }
        Event.EV_CASHLESS_VEND_DENIED -> {
            // Payment denied
        }
        // ... handle other events
    }
}
```

### 3.3 Payment Operations
```kotlin
// Send payment request
fun sendPaymentRequest(itemNumber: Int, itemPrice: Int) {
    vmController.executeItemSelected(itemNumber, itemPrice)
}

// Cancel vending
fun cancelVending(itemNumber: Int) {
    vmController.executeCancelVend(itemNumber)
}
```

---

## 4. UI Integration

### 4.1 VendingMachineScreen
A complete Compose screen demonstrating vending machine integration:

```kotlin
@Composable
fun VendingMachineScreen() {
    val context = LocalContext.current
    val mainActivity = context as MainActivity
    
    // Product selection and payment UI
    // Device status monitoring
    // Payment method selection
}
```

**Features:**
- Product selection with prices
- Device status monitoring
- Payment method selection (Card/Cash)
- Real-time status updates
- Cancel functionality

### 4.2 Usage Example
```kotlin
// In your UI component
Button(
    onClick = {
        mainActivity.sendPaymentRequest(productId, productPrice)
    },
    enabled = deviceReady
) {
    Text("Pay with Card")
}
```

---

## 5. MDB Protocol Details

### 5.1 VEND REQUEST Command Format
```
Byte 0: 0x13 (CASHLESS_VEND)
Byte 1: 0x00 (SUBCMD_VEND_REQUEST)
Byte 2: Price High Byte (bits 15-8)
Byte 3: Price Low Byte (bits 7-0)
Byte 4: Item Number High Byte (bits 15-8)
Byte 5: Item Number Low Byte (bits 7-0)
Byte 6: Checksum (sum of bytes 0-5)
```

### 5.2 Example: Send $2.50 for Product #3
```kotlin
val price = 250  // $2.50 in cents
val itemNumber = 3

// The CashLessDevice handles the MDB command construction
vmController.executeItemSelected(itemNumber, price)
```

---

## 6. Error Handling and Retry Logic

### 6.1 Transmission Error Handling
The implementation includes comprehensive error handling:

```kotlin
private fun isTransmissionSuccess(result: Int): Boolean {
    return when (result) {
        MdbMaster.ERR_FAIL, MdbMaster.ERR_WRITE -> false // Retry
        MdbMaster.ERR_NONRESP, MdbMaster.ERR_CHECKSUM, MdbMaster.ERR_READ -> false // Retry
        else -> true // Success
    }
}
```

### 6.2 Timeout Handling
Each device has configurable timeout settings:
- CashLessDevice: 5 seconds
- BillValidator: 2 seconds  
- CoinChanger: 2 seconds

---

## 7. Testing and Debugging

### 7.1 Logging
All MDB operations are logged with the tag "mdbMaster":

```kotlin
Log.d(Constant.TAG, "Sending MDB command: $commandString")
Log.d(Constant.TAG, "Received MDB response: $responseString")
```

### 7.2 Device Status Monitoring
Monitor device status through events:

```kotlin
// Check if devices are ready
val cashlessReady = // Check EV_CASHLESS_DETECTED event
val billValidatorReady = // Check EV_BILL_VALIDATOR_DETECTED event
val coinChangerReady = // Check EV_COIN_CHANGER_DETECTED event
```

---

## 8. Integration Checklist

### 8.1 Required Steps
- [x] Add CM30-HardwareLibrary-1.0.9.aar to project
- [x] Implement VendingMachineController singleton
- [x] Create CashLessDevice class with MDB protocol
- [x] Set up event handling system
- [x] Implement UI for product selection
- [x] Add error handling and retry logic
- [x] Test MDB communication
- [ ] Implement product dispensing mechanism (hardware specific)

### 8.2 Key Points to Remember
1. **Thread Safety**: Use synchronized blocks for shared resources
2. **Continuous Polling**: MDB devices require 100ms polling intervals
3. **Checksum Calculation**: Always include proper checksums in MDB commands
4. **Error Handling**: Implement retry logic for failed transmissions
5. **State Management**: Use state machines for device communication
6. **UI Updates**: Use Handler to update UI from background threads

---

## 9. Usage Examples

### 9.1 Basic Payment Flow
```kotlin
// 1. Initialize VMC (done in MainActivity)
val vmController = VendingMachineController.getInstance()
vmController.initialize(handler)
vmController.start()

// 2. Wait for device ready events
// EV_CASHLESS_DETECTED, EV_BILL_VALIDATOR_DETECTED, etc.

// 3. Send payment request
vmController.executeItemSelected(itemNumber = 1, itemPrice = 150) // $1.50

// 4. Handle payment response
// EV_CASHLESS_VEND_APPROVED -> dispense product
// EV_CASHLESS_VEND_DENIED -> show error

// 5. Complete transaction
// Product dispensing -> EV_CASHLESS_END_SESSION
```

### 9.2 Error Handling
```kotlin
// Handle payment denial
Event.EV_CASHLESS_VEND_DENIED -> {
    showError("Payment denied. Please try again.")
    resetProductSelection()
}

// Handle device errors
Event.EV_CASHLESS_MALFUNCTION -> {
    showError("Payment device error. Please contact support.")
    disablePaymentOptions()
}
```

---

## 10. Troubleshooting

### 10.1 Common Issues
1. **Device not detected**: Check MDB connection and power
2. **Payment timeouts**: Verify device response times
3. **Checksum errors**: Ensure proper command formatting
4. **Thread issues**: Use proper synchronization

### 10.2 Debug Commands
```kotlin
// Enable MDB logging (if supported by library)
mdbMaster.setLogLevel(1)

// Check device status
Log.d("VMC", "Cashless ready: ${cashlessDevice?.isOnline}")
Log.d("VMC", "Bill validator ready: ${billValidator?.isOnline}")
```

---

This implementation provides a complete foundation for vending machine integration in your Kotlin project. The key is understanding the MDB protocol communication flow and implementing proper state management for the payment devices.
