# Fingerprint Scanner Wrapper

### Scanner Wrapper

### Controllers and Helper Classes

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
