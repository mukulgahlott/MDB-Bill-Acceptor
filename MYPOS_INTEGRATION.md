# myPOS Payment Gateway Integration

This document describes the myPOS payment gateway integration implemented in the MdbBill application.

## Overview

The myPOS integration allows the application to process card payments using the myPOS Smart device. When a user clicks "Process Payment" in the AmountSelectionScreen, the myPOS payment gateway opens with the selected amount.

## Implementation Details

### 1. Dependencies

The myPOS SDK is included in the project:
```kotlin
implementation("com.mypos:mypossmartsdk:1.0.6")
```

### 2. AndroidManifest.xml Configuration

Added the required queries for myPOS package:
```xml
<queries>
    <package android:name="com.mypos" />
</queries>
```

### 3. Payment Gateway Implementation

The `MyPosGateway` class implements the `PaymentGateway` interface and handles:
- Payment initialization
- Payment request building
- Activity result handling
- Transaction result processing

### 4. Payment Flow

1. User selects an amount in `AmountSelectionScreen`
2. User clicks "Process Payment" button
3. `PaymentViewModel.processPayment()` validates the amount and sets pending payment state
4. `MainActivity` observes the payment state and calls `launchMyPosPayment()`
5. `MyPosGateway.startPaymentWithActivity()` builds the payment request with Activity context
6. `MyPOSAPI.openPaymentActivity()` opens the myPOS payment screen
7. User completes payment on the myPOS device
8. Result is returned via `onActivityResult()`
9. `MyPosGateway.handlePaymentResult()` processes the result
10. Success/failure is displayed to the user
11. MDB communication is triggered on successful payment

### 5. Payment Configuration

The payment request includes:
- Product amount (from user selection)
- Currency (EUR)
- Unique transaction ID
- Receipt printing (both merchant and customer)
- Mastercard and Visa branding
- Fixed pinpad keyboard

### 6. Result Handling

The integration handles various transaction results:
- **Success**: Transaction approved with response code "00"
- **Declined**: Transaction declined with specific response code
- **Cancelled**: User cancelled the transaction
- **Error**: Technical issues or invalid data

### 7. Logging

Comprehensive logging is implemented for debugging:
- Payment initiation
- Transaction results
- Response codes
- Card brands
- Authorization codes

## Usage

1. Ensure the myPOS Smart device is connected and configured
2. Select a payment amount in the app
3. Click "Process Payment"
4. Complete the payment on the myPOS device
5. View the result in the app

## Error Handling

The integration includes robust error handling for:
- SDK initialization failures
- Invalid amounts
- Network issues
- Device connectivity problems
- Transaction declines

## Testing

To test the integration:
1. Use test cards provided by myPOS
2. Verify receipt printing
3. Check MDB communication after successful payments
4. Test various decline scenarios

## Troubleshooting

Common issues and solutions:
- **Context is not an Activity**: Fixed by using Activity context from MainActivity
- **myPOS app not installed**: Check if myPOS app is available on the device
- **SDK not initialized**: Check device connectivity
- **Payment declined**: Verify card details and limits
- **Receipt not printed**: Check printer status
- **Activity result not received**: Verify request code handling 