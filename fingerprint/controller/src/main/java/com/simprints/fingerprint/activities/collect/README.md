# CollectFingerprintsActivity

This activity is the screen where the user scans fingerprint templates and images from the scanner.

The activity starts by receiving which fingers to capture in the `CollectFingerprintsTaskRequest`.
Once complete, it will return a list of `Fingerprint`s if it finishes normally.
Note that the `Fingerprints` returned may differ from that fingers requested if fingers were skipped or auto-added due to bad scans.

The activity UI centers around a `ViewPager` that holds `FingerFragment`s, with a `ScanningTimeoutBar` and button that get updated during the scanning flor.
The central state is stored as a `CollectFingerprintsState` by the view model, which is updated and re-posted whenever a change to the UI is to be made.
The appropriate UI is deduced mainly by retrieving from a map the current `FingerCollectionState`.
The view model also emits certain events peripherally.

`ScannerManager` is expected to already have a connected and ready Vero.
When the activity is resumed, the scanner trigger button is effectively linked to the scan button (except when the confirmation dialog is shown in which case it's linked to the "OK" option).
Should the scanner disconnect during communication, the `ConnectScannerActivity` will be launched with the reconnect mode, which will appear as an overlay.
The scanner disconnects upon leaving the activity.

`FingerprintCaptureEvent`s are saved in the current session for every scan.
If images were collected during the flow, they will be saved only when the activity finishes.

Pressing back yield the `RefusalActvity`. Submitting the refusal form will propagate through the activity.
The `AlertActivity` can be launched if an unexpected error occurred during scanning.
