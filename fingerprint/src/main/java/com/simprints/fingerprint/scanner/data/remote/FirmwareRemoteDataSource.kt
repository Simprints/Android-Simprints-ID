package com.simprints.fingerprint.scanner.data.remote

import com.simprints.fingerprint.controllers.core.network.FingerprintFileDownloader
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.scanner.domain.ota.DownloadableFirmwareVersion
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions

class FirmwareRemoteDataSource(
    private val fingerprintFileDownloader: FingerprintFileDownloader,
    private val preferencesManager: FingerprintPreferencesManager
) {

    /**
     * Allows for querying whether there are more up-to-date firmware versions by sending the currently saved versions
     */
    fun getDownloadableFirmwares(hardwareVersion: String,
                                 localFirmwareVersions: ScannerFirmwareVersions)
    : List<DownloadableFirmwareVersion> =
        preferencesManager.scannerRevisions.availableForDownload(
            hardwareVersion,
            localFirmwareVersions
        )

    /**
     * Downloads the firmware binary at the given URL
     */
    suspend fun downloadFile(url: String) = fingerprintFileDownloader.download(url)
}
