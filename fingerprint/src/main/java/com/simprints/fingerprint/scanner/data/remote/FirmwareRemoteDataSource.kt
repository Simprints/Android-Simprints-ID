package com.simprints.fingerprint.scanner.data.remote

import com.simprints.fingerprint.controllers.core.network.FingerprintFileDownloader
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.scanner.domain.ota.DownloadableFirmwareVersion
import com.simprints.logging.Simber

class FirmwareRemoteDataSource(
    private val fingerprintFileDownloader: FingerprintFileDownloader,
    private val preferencesManager: FingerprintPreferencesManager,
) {

    /**
     * Allows for querying whether there are more up-to-date firmware versions by sending the currently saved versions
     */
    fun getDownloadableFirmwares(
        hardwareVersion: String,
        localFirmwareVersions: Map<DownloadableFirmwareVersion.Chip, Set<String>>
    ): List<DownloadableFirmwareVersion> =
        preferencesManager.scannerHardwareRevisions.getAvailableVersionsForDownload (
            hardwareVersion,
            localFirmwareVersions
        )

    /**
     * Downloads the firmware binary at the given URL
     */
    suspend fun downloadFirmware(firmwareVersion: DownloadableFirmwareVersion): ByteArray {
        val fileUrl = fingerprintFileDownloader.getFileUrl(firmwareVersion.toStringForApi())
        Simber.d("Downloading firmware file at %s", fileUrl)
        return fingerprintFileDownloader.download(fileUrl)
    }


}
