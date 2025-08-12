# MDB Bill - Android Payment Terminal

A professional Android application for MDB (Multi-Drop Bus) payment processing, designed for CM30 devices with support for multiple payment processors.

## Features

### 🏠 Welcome Screen
- **Beautiful gradient UI** with professional design
- **Hidden admin access** - Long press the logo 5 times to access admin panel
- **Main payment button** for quick access to payment processing
- **Status indicator** showing payment terminal readiness

### 🔐 Admin Panel
- **Password-protected access** with 4-digit PIN (default: 1234)
- **Comprehensive settings** for all application parameters
- **Operating mode selection** (LIVE/TRAINING)
- **Payment processor selection** (myPOS/Revolut)
- **Predefined amounts** configuration (3 customizable amounts)
- **Amount limits** (minimum/maximum)
- **API key management** for payment processors
- **Connection testing** for MDB hardware
- **App reset functionality**

### 💳 Payment Processing
- **Amount selection screen** with predefined and custom amounts
- **Real-time payment processing** with loading indicators
- **Payment result display** with success/error handling
- **MDB communication** after successful payments
- **Training mode support** for testing without actual MDB communication

### 🔧 Technical Features
- **Auto-launch on boot** for unattended operation
- **Portrait orientation lock** for consistent UI
- **Screen wake lock** to prevent sleep during operation
- **Optimized performance** with modern Android architecture

## Architecture

### Technology Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Navigation**: Jetpack Navigation Component
- **Data Storage**: SharedPreferences
- **Hardware Integration**: CM30-HardwareLibrary-1.0.9.aar

### Project Structure
```
app/src/main/java/com/example/mdbbill/
├── data/
│   └── AppSettings.kt              # Settings management
├── mdb/
│   └── MdbController.kt            # MDB hardware communication
├── navigation/
│   └── NavGraph.kt                 # Navigation setup
├── payment/
│   └── PaymentGateway.kt           # Payment processor interfaces
├── ui/screens/
│   ├── WelcomeScreen.kt            # Main entry screen
│   ├── PasswordScreen.kt           # Admin authentication
│   ├── AdminPanelScreen.kt         # Settings configuration
│   └── AmountSelectionScreen.kt    # Payment amount selection
├── viewmodel/
│   ├── AdminViewModel.kt           # Admin panel state management
│   └── PaymentViewModel.kt         # Payment processing state
├── BootReceiver.kt                 # Auto-launch functionality
└── MainActivity.kt                 # Main application entry
```

## Installation & Setup

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 29+ (API level 29)
- CM30 device for hardware integration

### Build Instructions
1. Clone the repository
2. Open the project in Android Studio
3. Ensure the CM30-HardwareLibrary-1.0.9.aar is in `app/libs/`
4. Sync Gradle dependencies
5. Build and install on target device

### Configuration
1. **First Launch**: The app will start with default settings
2. **Admin Access**: Long press the logo 5 times on the welcome screen
3. **PIN Setup**: Default admin PIN is "1234" (change in admin panel)
4. **Payment Processors**: Configure API keys for myPOS and/or Revolut
5. **Amounts**: Set predefined amounts and limits as needed
6. **Operating Mode**: Choose between LIVE and TRAINING modes

## Usage

### Normal Operation
1. **Start the app** - It will auto-launch on boot if configured
2. **Select payment** - Tap "Pay by Card" on the welcome screen
3. **Choose amount** - Select from predefined amounts or enter custom amount
4. **Process payment** - Tap "Process Payment" to initiate transaction
5. **Complete transaction** - Payment processor handles the transaction
6. **MDB communication** - On success, the app communicates with MDB hardware

### Admin Operations
1. **Access admin panel** - Long press logo 5 times, then enter PIN
2. **Configure settings** - Modify amounts, limits, and processor settings
3. **Test connections** - Verify MDB hardware connectivity
4. **Switch modes** - Toggle between LIVE and TRAINING modes

## Payment Processors

### myPOS Integration
- **SDK Integration**: Ready for myPOS SDK implementation
- **API Key Configuration**: Set in admin panel
- **Payment Flow**: Simulated for development (replace with actual SDK calls)

### Revolut Integration
- **SDK Integration**: Ready for Revolut SDK implementation
- **API Key Configuration**: Set in admin panel
- **Payment Flow**: Simulated for development (replace with actual SDK calls)

## MDB Communication

### Hardware Integration
- **CM30 Library**: Uses CM30-HardwareLibrary-1.0.9.aar
- **Communication Protocol**: MDB standard protocol
- **Response Handling**: Sends amount in cents and ACK response
- **Error Handling**: Comprehensive error logging and recovery

### Operating Modes
- **LIVE Mode**: Full MDB communication with actual hardware
- **TRAINING Mode**: Simulated communication for testing

## Security Features

- **Admin PIN Protection**: 4-digit PIN for admin access
- **Settings Encryption**: All settings stored securely
- **API Key Protection**: Secure storage of payment processor keys
- **Training Mode**: Safe testing without live transactions

## Troubleshooting

### Common Issues
1. **App won't start**: Check boot receiver permissions
2. **Payment fails**: Verify API keys and network connectivity
3. **MDB communication errors**: Test connection in admin panel
4. **Admin access denied**: Verify PIN and long press count

### Debug Information
- **Logs**: Check Android logs for detailed error information
- **Connection Test**: Use admin panel to test MDB connectivity
- **Training Mode**: Use for testing without live hardware

## Development Notes

### TODO Items
- [ ] Integrate actual myPOS SDK
- [ ] Integrate actual Revolut SDK
- [ ] Implement actual CM30-HardwareLibrary methods
- [ ] Add more payment processors
- [ ] Implement transaction logging
- [ ] Add receipt printing functionality

### Customization
- **UI Colors**: Modify color schemes in `colors.xml`
- **Amounts**: Configure predefined amounts in admin panel
- **Payment Processors**: Add new processors in `PaymentGateway.kt`
- **MDB Protocol**: Customize communication in `MdbController.kt`

## License

This project is proprietary software. All rights reserved.

## Support

For technical support or feature requests, please contact the development team.

---

**Version**: 1.0  
**Last Updated**: 2024  
**Compatibility**: Android 10+ (API 29+)  
**Hardware**: CM30 Devices 