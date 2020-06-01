This sub-package handle the downloading of the firmware binaries from the cloud:

1. Every time the fingerprint `OrchestratorActivity` is called, the `FirmwareFileUpdateScheduler` schedules the `FirmwareFileUpdateWorker`
2. `FirmwareFileUpdateWorker` calls `FirmwareFileUpdater`. If this fails for whatever reason, it is retried with Android’s default exponential back-off retry policy.
3. `FirmwareFileUpdater` uses `FirmwareFileManager` to check which current versions are on the phone
4. It then makes a request to the back-end via `FirmwareRemoteInterface` to get the URLs of downloadable binaries if there are any.
5. It then calls `FingerprintFileDownloader` to download the binaries with the URL (which is a short lived URL).
6. It then saved the binaries with `FirmwareFileManager`. Any old binaries are deleted.
7. With this flow, the phone always has the latest version of each binary stored. The next time the scanner connects to the phone, if the version on the scanner is less than the stored version, the OTA update commences.
