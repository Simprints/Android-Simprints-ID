# SimulatedScanner

This library allows you to simulated entirely the Android Framework `BluetoothAdapter` object, and pair simulated Simprints scanners that mimic the real one for use in Android instrumented tests. The simulated occurs at the level of the bluetooth socket. The scanner will wait for bytes to be sent to it and respond immediately with an appropriate response.

It is important that in production code, the interface `libscanner.bluetooth.BluetoothComponentAdapter` and class `libscanner.bluetooth.android.AndroidBluetoothAdapter` is used to wrap the real `android.bluetooth.BluetoothAdapter` like so:
```kotlin
open fun provideBluetoothComponentAdapter(): BluetoothComponentAdapter = AndroidBluetoothAdapter(BluetoothAdapter.getDefaultAdapter())
```
You can then proceed to use `BluetoothComponentAdapter` as a `BluetoothAdapter` in production code. This allows you to override the above function when writing Android instrumented tests, and instead swap out for a `SimulatedBluetoothAdapter`:
```kotlin
override fun provideBluetoothComponentAdapter(): BluetoothComponentAdapter = SimulatedBluetoothAdapter(SimulatedScannerManager())
```

You can now configure the simulated behaviour of the bluetooth component with `SimulatedScannerManager`, such as having bluetooth disabled, or having multiple scanners paired. The default constructor with no parameters will create an enabled bluetooth adapter that has a single paired scanner that alternates between returning two good scanned fingers when prompted to scan.

To queue individual fingers you can use the following syntax:
```kotlin
SimulatedScannerManager(simulatedFingers = arrayOf(
    SimulatedFinger.PERSON_1_VERSION_1_LEFT_THUMB_BAD_SCAN,
    SimulatedFinger.PERSON_1_VERSION_1_LEFT_THUMB_GOOD_SCAN,
    SimulatedFinger.PERSON_1_VERSION_1_LEFT_INDEX_GOOD_SCAN,
    SimulatedFinger.NO_FINGER))
```
`SimulatedFinger` also contains some convenience fields that contain arrays of fingers that go together that you can spread like this:
```kotlin
SimulatedScannerManager(simulatedFingers = arrayOf(
   *SimulatedFinger.person1TwoFingersGoodScan,
   *SimulatedFinger.person1TwoFingersAgainGoodScan,
   *SimulatedFinger.person1TwoFingersAgainGoodScan))
```
Once the manager reaches the end of the `simulatedFingers` queue, it will loop back to the beginning.

The manager can also imitate other behaviours, and starts with the following default values:
```kotlin
SimulatedScannerManager(val simulatedFingers: Array<SimulatedFinger> = SimulatedFinger.person1TwoFingersGoodScan,
                        private val pairedScannerAddresses: Set<String> = setOf(DEFAULT_MAC_ADDRESS),
                        var isAdapterNull: Boolean = false,
                        var isAdapterEnabled: Boolean = true,
                        var isDeviceBonded: Boolean = true,
                        var deviceName: String = "")
```
