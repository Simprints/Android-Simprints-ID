# Fingerprint Scanner Mock

This package contains alternatives for
`android.bluetooth.BluetoothAdapter` and associated classes that are
designed for use with the
[Bluetooth Component Abstractions](../fingerprintscanner/src/main/java/com/simprints/fingerprintscanner/component/bluetooth/)
for mocking, debugging, and testing. The options are as follows:

- [Simulated Bluetooth Adapter](./src/main/java/com/simprints/fingerprintscannermock/simulated/README.md)
  \- This is for an elaborate mock of the Vero fingerprint scanner, that
  simulates responses and interaction faithful to how the real scanner
  would operate. For use in integration testing or for manual
  testing/debugging code easily or where we want the scanner to always
  respond in a certain way.
- [Dummy Bluetooth Adapter](./src/main/java/com/simprints/fingerprintscannermock/dummy/README.md)
  \- This is a very simple skeleton that can be used in tests where
  communication with the scanner does not occur, but we just want
  something simple to pass as the `ComponentBluetoothAdapter` to allow
  code to compile.
- [Android Record Bluetooth Adapter](./src/main/java/com/simprints/fingerprintscannermock/record/README.md)
  \- This uses the actual `android.bluetooth.BluetoothAdapter` but with
  ability to record the received byte streams into a
  file for debugging purposes.
