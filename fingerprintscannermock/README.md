# MockScanner

This library allows you to mock entirely the Android Framework `BluetoothAdapter` object, and pair mocked Simprints scanners that mimick the real one for use in Android instrumented tests. The mocking occurs at the level of the bluetooth socket. The scanner will wait for bytes to be sent to it and respond immediately with an appropriate response.

It is important that in production code, the interface `libscanner.bluetooth.BluetoothComponentAdapter` and class `libscanner.bluetooth.android.AndroidBluetoothAdapter` is used to wrap the real `android.bluetooth.BluetoothAdapter` like so:
```kotlin
open fun provideBluetoothComponentAdapter(): BluetoothComponentAdapter = AndroidBluetoothAdapter(BluetoothAdapter.getDefaultAdapter())
```
You can then proceed to use `BluetoothComponentAdapter` as a `BluetoothAdapter` in production code. This allows you to override the above function when writing Android instrumented tests, and instead swap out for a `MockBluetoothAdapter`:
```kotlin
override fun provideBluetoothComponentAdapter(): BluetoothComponentAdapter = MockBluetoothAdapter(MockScannerManager())
```

You can now configure the mocking behaviour of the bluetooth component with `MockScannerManager`, such as having bluetooth disabled, or having multiple scanners paired. The default constructor with no parameters will create an enabled bluetooth adapter that has a single paired scanner that alternates between returning two good scanned fingers when prompted to scan.

To queue individual fingers you can use the following syntax:
```kotlin
MockScannerManager(mockFingers = arrayOf(
    MockFinger.PERSON_1_VERSION_1_LEFT_THUMB_BAD_SCAN,
    MockFinger.PERSON_1_VERSION_1_LEFT_THUMB_GOOD_SCAN,
    MockFinger.PERSON_1_VERSION_1_LEFT_INDEX_GOOD_SCAN,
    MockFinger.NO_FINGER))
```
`MockFinger` also contains some convenience fields that contain arrays of fingers that go together that you can spread like this:
```kotlin
MockScannerManager(mockFingers = arrayOf(
   *MockFinger.person1TwoFingersGoodScan,
   *MockFinger.person1TwoFingersAgainGoodScan,
   *MockFinger.person1TwoFingersAgainGoodScan))
```
Once the manager reaches the end of the `mockFingers` queue, it will loop back to the beginning.

The manager can also immitate other behaviours, and starts with the following default values:
```kotlin
MockScannerManager(val mockFingers: Array<MockFinger> = MockFinger.person1TwoFingersGoodScan,
                   private val pairedScannerAddresses: Set<String> = setOf(DEFAULT_MAC_ADDRESS),
                   var isAdapterNull: Boolean = false,
                   var isAdapterEnabled: Boolean = true,
                   var isDeviceBonded: Boolean = true,
                   var deviceName: String = "")
```
