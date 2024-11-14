# Scanner Mock Module README

The Scanner Mock Module provides alternative Bluetooth adapters for replacing real Vero2 or Vero1 scanners in testing environments. This module includes adapters to simulate fingerprint capturing scenarios, record Bluetooth data for debugging, or serve as simple placeholders in basic tests. This README provides guidance on setting up and using each of these mock adapters in your testing environment.

---

## 1. Replacing the Real Android Bluetooth Adapter

To replace the real `AndroidBluetoothAdapter` with one of the mock adapters, select the appropriate adapter for your test case:
- Substitute the real `AndroidBluetoothAdapter` with `SimulatedBluetoothAdapter` in your DI setup. [Example](../../connect/src/main/java/com/simprints/fingerprint/connect/ScannerConnectModule.kt).
### A. **SimulatedBluetoothAdapter**

Use the `SimulatedBluetoothAdapter` to fully simulate the fingerprint capturing scenario in NEC or SimMatcher flows. This adapter is designed to emulate the complete scanning and capturing process, providing success and error responses similar to those from real scanners.

#### Setup
- Configure the test data in `SimulatedUn20ResponseHelper` to match your test case needs. This helper currently contains all necessary data for a successful fingerprint collection flow, but you can adjust it for specific scenarios or errors.

#### Reference
More details on using the `SimulatedBluetoothAdapter` can be found in the [README](../scannermock/src/main/java/com/simprints/fingerprint/infra/scannermock/simulated/README.md).

### B. **AndroidRecordBluetoothAdapter**

The `AndroidRecordBluetoothAdapter` mimics the real Android Bluetooth components with the added functionality of recording incoming data bytes from the scanner to a file. This is particularly useful for debugging purposes, allowing you to analyze the exact data transmitted during the fingerprint scanning process.
More details on using the `AndroidRecordBluetoothAdapter` can be found in the [README](../scannermock/src/main/java/com/simprints/fingerprint/infra/scannermock/record/README.md).
### C. **DummyBluetoothAdapter**

The `DummyBluetoothAdapter` serves as a simple placeholder adapter, useful in tests that require a Bluetooth component but do not involve actual data transmission or device communication.

- This adapter is beneficial when:
    - You need a `ComponentBluetoothAdapter` instance that is enabled.
    - You need access to the MAC address of the `ComponentBluetoothDevice`.

> **Note**: Attempts to connect or communicate with the `DummyBluetoothAdapter` will throw an `UnsupportedOperationException`.

---

## Summary

Each mock adapter provides specific functionality suited to different test requirements:

| Adapter                    | Purpose                                               |
|----------------------------|-------------------------------------------------------|
| **SimulatedBluetoothAdapter**       | Full simulation of the fingerprint capture process |
| **AndroidRecordBluetoothAdapter**   | Real Bluetooth functionality with data recording   |
| **DummyBluetoothAdapter**           | Basic placeholder with minimal Bluetooth properties|

Choose the adapter that best fits your test scenario to effectively mock the scanner behavior and ensure accurate and efficient testing.
