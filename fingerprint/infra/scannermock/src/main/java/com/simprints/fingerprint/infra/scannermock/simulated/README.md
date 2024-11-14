# Simulated Scanner

This package is for simulating the Vero fingerprint scanner for use in
testing, mocking, and debugging. It relies on the
[Component Bluetooth Abstractions](../../../../../../../../fingerprintscanner/src/main/java/com/simprints/fingerprintscanner/component/bluetooth/)
being used in place of Android Bluetooth classes.

It works by reading all bytes that are trying to be sent over Bluetooth,
interpreting them, and sending back an appropriate response. It aims to
create a faithful reproduction of the behaviour and communication that
would be exhibited by an actual Vero. Desired behaviour and
configurability is provided by the main class,
[`SimulatedScannerManager`](./SimulatedScannerManager.kt)

## Usage

To use the package in tests or for debugging, it is important to inject
a
[`SimulatedBluetoothAdapter`](./component/SimulatedBluetoothAdapter.kt)
instead of an `android.bluetooth.BluetoothAdapter` in place of the
`ComponentBluetoothAdapter` using whatever dependency injection
framework or paradigm. The `SimulatedBluetoothAdapter` takes a
[`SimulatedScannerManager`](./SimulatedScannerManager.kt), which
contains all the options for configurability. It takes the following
arguments with defaults:

```kotlin
class SimulatedScannerManager(
    val simulationMode: SimulationMode,
    val initialScannerState: SimulatedScannerState? = null,
    val simulationSpeedBehaviour: SimulationSpeedBehaviour = SimulationSpeedBehaviour.INSTANT,
    val simulatedFingers: Array<SimulatedFinger> = SimulatedFinger.person1TwoFingersGoodScan,
    val pairedScannerAddresses: Set<String> = setOf(DEFAULT_MAC_ADDRESS),
    var isAdapterNull: Boolean = false,
    var isAdapterEnabled: Boolean = true,
    var isDeviceBonded: Boolean = true,
    var deviceName: String = "",
    var outgoingStreamObservers: Set<Observer<ByteArray>> = setOf()
)
```

- `simulationMode` - The [SimulationMode](./SimulationMode.kt)
  determines which generation of Vero to simulate.
    - `SimulationMode.V1` will simulate a Vero 1
    - `SimulationMode.V2` will simulate a Vero 2
- `initialScannerState` - The beginning `SimulatedScannerState`. Default
  (`null`) will create a disconnected scanner waiting for connection.
- `simulationSpeedBehaviour` - How much delay should the simulation add
  before responding to messages.
    - `SimulationSpeedBehaviour.INSTANT` will add no delay at all between
    receiving a command and returning the response, allowing for very
    fast communication. This is ideal for unit and integration tests.
    This setting is the default.
    - `SimulationSpeedBehaviour.REALISTIC` will add a delay between
    receiving a command and returning a response depending on the
    command, allowing for a realistic feel for the simulated scanner.
    Additionally, time will be added between Bluetooth packets. This is
    ideal for User-Acceptance Testing or for load-testing the
    `fingerprintscanner` module code.
- `simulatedFingers` - An array of
  [`SimulatedFinger`s](./common/SimulatedFinger.kt) which are the
  fingerprints that will be returned by the simulation upon successive
  scans. The first scan will return a fingerprint corresponding to the
  first `SimulatedFinger` in the array, the second scan will return a
  fingerprint corresponding the second in the array, and so on. Upon
  reaching the end of the array, subsequent scans will loop to the
  beginning of the array.
    - The default is `SimulatedFinger.person1TwoFingersGoodScan`.
    - Multiple versions of the same finger can be used in tests for
    successful matching with good match scores during testing, or
    different people/fingers can be used for low match scores.
    - There are options for good scans and poor scans that yield different
    quality scores.
    - There is an option for if no finger is detected by the scanner at
    all, `SimulatedFinger.NO_FINGER`.
- `pairedScannerAddresses` - This is a set of MAC addresses that the
  will act as if they are paired to the Bluetooth adapter. By default,
  there is one valid paired MAC address that corresponds to a Vero 1.
- `isAdapterNull` - On emulators and some rare/old devices, there is no
  Bluetooth adapter and calls to
  `android.bluetooth.BluetoothAdapter.getDefaultAdapter()` will return
  `null`. This flag is emulate this behaviour, by default it is `false`.
- `isAdapterEnabled` - This is whether Bluetooth is switched on. Default
  is `true`.
- `isDeviceBonded` - This is for whether the `SimulatedBluetoothDevice`
  is paired (this bears no relation to `pairedScannerAddresses`).
  Defaults to `true`.
- `deviceName` - This is the Bluetooth name of the
  `SimulatedBluetoothDevice`. Defaults to the empty string.
- `outgoingStreamObservers` - This is for adding any additional
  observers to the Bluetooth output stream for debugging purposes.
