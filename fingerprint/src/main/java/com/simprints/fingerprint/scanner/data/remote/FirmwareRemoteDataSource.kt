package com.simprints.fingerprint.scanner.data.remote

import com.simprints.fingerprint.controllers.core.network.FingerprintFileDownloader
import com.simprints.fingerprint.scanner.domain.ota.DownloadableFirmwareVersion
import com.simprints.fingerprint.scanner.domain.versions.getAvailableVersionsForDownload
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.logging.Simber

class FirmwareRemoteDataSource(
    private val fingerprintFileDownloader: FingerprintFileDownloader,
    private val configManager: ConfigManager,
) {

    /**
     * Allows for querying whether there are more up-to-date firmware versions by sending the currently saved versions
     */
    suspend fun getDownloadableFirmwares(
        hardwareVersion: String,
        localFirmwareVersions: Map<DownloadableFirmwareVersion.Chip, Set<String>>
    ): List<DownloadableFirmwareVersion> =
        configManager.getProjectConfiguration().fingerprint?.vero2?.firmwareVersions?.getAvailableVersionsForDownload(
            hardwareVersion,
            localFirmwareVersions
        ) ?: listOf()

    /**
     * Downloads the firmware binary at the given URL
     */
    suspend fun downloadFirmware(firmwareVersion: DownloadableFirmwareVersion): ByteArray {
        val fileUrl = fingerprintFileDownloader.getFileUrl(firmwareVersion.toStringForApi())
        Simber.d("Downloading firmware file at %s", fileUrl)
        return fingerprintFileDownloader.download(fileUrl)
    }


}
