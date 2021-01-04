# Scanner Package

This package provides a layer with which to interface with the
[`fingerprintscanner`](../../../../../../../../fingerprintscanner)
library.

### Scanner Wrapper

The [`ScannerWrapper`](./wrapper/ScannerWrapper.kt) interface acts as a
common abstraction behind different generations of the scanner. It has
two implementations, [`ScannerWrapperV1`](./wrapper/ScannerWrapperV1.kt)
corresponding to Vero 1 and
[`ScannerWrapperV2`](./wrapper/ScannerWrapperV2.kt) for Vero 2. These
implementations handle the adapting of the different `Scanner` objects
in `fingerprintscanner` to conform to a common interface. For many
methods, multiple calls to a `Scanner` object may be invoked for each
single method on the `ScannerWrapper`.

Many features, such as OTA and image transfer, are available for Vero 2
but not Vero 1, so calling these will result in an
`UnavailableVero2FeatureException`. Care must be taken to ensure a
project is correctly configured so this cannot happen.

Additionally, some features such as live feedback are only available on
later versions of Vero 2 and thus careful checking e.g. with
`scannerWrapper.isLiveFeedbackAvailable()` is needed to ensure these are
not called on hardware that does not support the feature.

### Scanner Manager

The [`ScannerManager`](./ScannerManager.kt)'s primary purpose is the
main class that is passed around the code via dependency injection. It's
primary purpose is to act as a holder for the `ScannerWrapper`, and is
designed to be a singleton so that the `ScannerWrapper` can be connected
in one Activity, and used in another.

It also instantiates the appropriate `ScannerWrapper` upon
`scannerManager.initScanner()` based on currently paired MAC addresses.
Not that this method does not connect the scanner, but simply creates
the proper `ScannerWrapper` instance.

### Controllers and Helper Classes

To avoid `ScannerWrapperV2` from growing too large, a lot of controlling
logic for Vero 2 is factored into [other classes](./controllers/v2/):

- [`ConnectionHelper`](./controllers/v2/ConnectionHelper.kt) - This is
  for handling connecting and disconnecting to the scanner, as well as
  holding the reference to the `ComponentBluetoothSocket`.
- [`ScannerInitialSetupHelper`](./controllers/v2/ScannerInitialSetupHelper.kt)
  \- This class is for handling the initial setup to the scanner upon
  connection, such as retrieving and checking the firmware version and
  battery level, and determining whether OTA is necessary (see
  [Update Triggering](#update-triggering)).
- OTA Update Helpers - These classes are for conducting the OTA steps
  required for each of the three chips (see
  [OTA Update Flow](#ota-update-flow)).

In use by the `ScannerManager` and throughout other parts of the code
are some helper classes and tools:

- [`ScannerFactory`](./factory/ScannerFactory.kt) - For creating the
  `ScannerWrapper` object itself once the MAC address to use has been
  determined.
- [`ScannerPairingManager`](./pairing/ScannerPairingManager.kt) - For
  programmatic pairing of the scanner, helper functions for MAC
  addresses, and callbacks for once the scanner is paired.
- [`ScannerUiHelper`](./ui/ScannerUiHelper.kt) - (For Vero 2 only) For
  determining the correct LED state to send to Vero 2 based on different
  situations.
- [`ScannerGenerationDeterminer`](./tools/ScannerGenerationDeterminer.kt)
  \- Helper class for determining which generation of Vero a particular
  MAC address corresponds to.
- [`SerialNumberConverter`](./tools/SerialNumberConverter.kt) - Helper
  class for converting back and forth between Vero serial numbers and
  MAC addresses.

## Vero 2 Versioning and OTA

### Versioning System

Vero 2 comes with 3 chips (see the documentation for
[Scanner V2](../../../../../../../../fingerprintscanner/src/main/java/com/simprints/fingerprintscanner/v2/README.md)
for details), each with its own app and API versions.

- The app version is of type `[Major].[Minor]` and refers to the version
  of the firmware binary running on the chip. Increments in minor
  version number indicates updates in firmware that maintains
  compatibility between chips. Increments in major version breaks mutual
  compatibility between chips, so all chips need to have corresponding
  major versions to work without error.
- The API version is also of type `[Major].[Minor]` and refers to the
  version of the Bluetooth API specification which indicates which
  commands it is capable of receiving. Minor version changes indicate
  changes that are backward compatible and represent only additions to
  the API, such as a new command. Major version changes indicate
  breaking changes from the previous version.

Each chip can be asked its own version numbers directly. In addition to
this, there exists a Unified Version which includes both the app and API
versions of each of the three chips as well as a master version number.
The master version number is a mathematical combination of all the
version numbers of the other three chips and is strictly increasing when
any one version number increases.

### OTA Updates

#### Downloading and Storing Firmware Binaries

The [data](./data) package concerns the downloading and storing of
firmware binaries. See the [README](./data/README.md) for a summary on
the steps for downloading and storing these binaries. In short, workers
running periodically ensure that the latest firmware files are always
present on the phone.

#### Update Triggering

When connection with a Vero 2 is established, during the
`ScannerInitialSetupHelper` flow, the Unified Version information is
read and the firmware version of each chip is compared with that of the
saved firmware binaries on the device. If an update is available and
some other criteria (e.g. battery level) are satisfied, then the
`ScannerInitialSetupHelper` throws an exception interrupting the setup
and letting the user proceed with the OTA update flow.

#### OTA Update Flow

Each of the chips have different procedures before and after the actual
firmware binary is sent to the scanner. These are defined in
[`OtaStep`](./domain/ota/OtaStep.kt) and conducted by the three
[`OtaHelper`s](./controllers/v2/). In addition to the OTA itself, these
steps involve various dances of reconnecting, mode switching, and
updating the Unified Version information after the update.

#### OTA Failure

If an error, disconnect, or unexpected response occur at any stage
during an OTA flow, each `OtaStep` is equipped with a particular
[`OtaRecoveryStrategy`](./domain/ota/OtaRecoveryStrategy.kt). This will
involve prompting the user to conduct either a soft or a hard reset. A
soft reset is simply turning the scanner off and on again (note that
when the scanner is "off" it is actually on but at a very low clock
speed). A hard reset involves pressing the power button for 5+ seconds
until the lights flash. This properly restarts all chips.
