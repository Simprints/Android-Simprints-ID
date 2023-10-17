package com.simprints.fingerprint.infra.scanner.data.remote

import com.simprints.fingerprint.infra.scanner.data.remote.network.FingerprintFileDownloader
import com.simprints.fingerprint.infra.scanner.domain.ota.DownloadableFirmwareVersion
import com.simprints.fingerprint.infra.scanner.domain.versions.getAvailableVersionsForDownload
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.logging.Simber
import javax.inject.Inject

/**
 * This class serves as the remote data provider of the Firmware versions.
 *
 * @property fingerprintFileDownloader  the file downloader responsible for downloading the updated firmware
 */
class FirmwareRemoteDataSource @Inject constructor(
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
