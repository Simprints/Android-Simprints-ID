# ConnectScannerActivity

This is the activity that handles the setup with the scanner, and any associated issues that might occur.
The desired side-effect of this activity is creating a connected instance of `Scanner` in the `ScannerManager` singleton, which can then be used by proceeding activities.

It is a single Activity design using the [Navigation Architecture Component](https://developer.android.com/guide/navigation) to switch between fragments.
There is a shared view model for all fragments, `ConnectScannerViewModel`, which handles the setup flow with the scanner. The more complex fragments additionally have their own view models.
The primary fragment is `ConnectScannerMainFragment`. During the setup flow, `FingerpintAlert`s can be triggered by the `ScannerManager`.
Most of these are turned into `ConnectScannerIssue`s, which represents a particular screen to show to the user for them to deal with.
There is a fragment for each issue, and they can be launched from the main fragment during the flow and sometimes launch each other.

After connecting is successful, automatically transitions to the `CollectFingerprintsActivity` via the `Orchestrator`.

Pressing the back button at any time triggers the exit form (`RefusalActivity`), and there should be no issue returning to this activity if the user changes their mind.

Some `FingerprintAlert`s can still trigger the `AlertActivity`. These are:
- `BLUETOOTH_NOT_SUPPORTED` - we didn't bother to update the UI for this as it's so rare (and inconceivable in production)
- `LOW_BATTERY` - this can currently only be triggered by Vero 1, and in practice was never seen, so left as is
- `UNEXPECTED_ERROR` - this occurs when an irrecoverable programmatic error has occurred

### BluetoothOffFragment
This can be launched at the start of the flow when bluetooth is off.
The button programmatically turns bluetooth on.
This makes use of the `BLUETOOTH_ADMIN` permission, which appears to be granted automatically upon install.

Returns to the `ConnectScannerMainFragment` upon finish.

### NfcOffFragment
This can be launched when we want to reach the `NfcPairIssue` but NFC is off on the device.
There is no way to programmatically turn on NFC, so an `Intent` is launched to the settings screen where NFC resides and we check upon resume if NFC is on.

Always leads into the `NfcPairFragment` upon finish.

### ScannerOffFragment
This is launched after the user has confirmed the scanner's serial number but it can't be connected to.
The default assumption is that it is off, so the user is prompted to turn it on.

Whilst this activity is created, the `ConnectScannerViewModel` is retrying the connect indefinitely in the background.
A "Try Again" button exists, but does nothing.
The user is provided with a "link" in case we are trying to connect to the wrong scanner and got to this screen by accident.

Upon successful connection, always directly finishes the activity.
If the user presses the "SPXX is not my scanner" link, this leads to the appropriate pairing fragment, which can be either `NfcOffFragment`, `NfcPairFragment`, or the `SerialEntryFragment`.

### NfcPairFragment
This fragment is launched if the user confirms the wrong scanner is paired, if there are no scanners paired, or if there are multiple scanners paired.
Additionally, this fragment is launched only on projects that are not using Vero 1 and only on devices that have an NFC adapter.
Whilst started, detected NFC chips and reads them for the Simprints MAC address.
Once an NFC chip is detected with a valid MAC address, all other Simprints scanners are programmatically unpaired and the we pair only to the provided scanner.

When pairing, the fragment is listening for `BluetoothDevice.ACTION_BOND_STATE_CHANGED` to detect if the bond was successful.
Even if pairing is successful, this action sometimes never activates (suspected bug in Android on some devices that can be fixed by restarting the device).
For this reason, there is additionally a timeout to check whether the pairing was successful.

The pairing will fail if the device was off. In this case, the user is prompted to turn the device on and try again.

If the user is struggling to detect the NFC chip, there is link to the `SerialEntryPairFragment`.

Upon successful pairing, restarts connecting and returns to the `ConnectScannerMainFragment`.

### SerialEntryPairFragment
This fragment is the fallback to `NfcPairFragment`, and is triggered when the user confirms the wrong scanner is paired, if there are no scanners paired, or if there are multiple scanners paired, but in projects using Vero 1 or if the phone does not have an NFC adapter.
Can additionally be triggered by the `NfcPairFragment` if the user had difficulty detecting the NFC chip.

The user can enter a serial number as a stand-in for the NFC chip in the `NfcPairFragment`, and the consequent pairing, listening for bond state action, retry, and timeout behaviour is identical.

Upon successful pairing, restarts connecting and returns to the `ConnectScannerMainFragment`.
