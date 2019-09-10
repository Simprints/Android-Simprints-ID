package com.simprints.fingerprintscanner.v2.bluetooth

import java.io.IOException

sealed class BluetoothException(message: String) : RuntimeException(message)
class BluetoothNotSupportedException : BluetoothException("Bluetooth not supported on this platform")
class BluetoothNotEnabledException : BluetoothException("Bluetooth not currently enabled")
class BluetoothDeviceNotPairedException(macAddress: String) : BluetoothException("Bluetooth device not currently paired with MAC Address $macAddress")
class BluetoothConnectionException(cause: IOException) : BluetoothException("Bluetooth connection failed. Reason: ${cause.message}")
