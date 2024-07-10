package com.simprints.fingerprint.infra.scanner.data.remote

import com.simprints.fingerprint.infra.scanner.data.remote.network.FingerprintFileDownloader
import com.simprints.fingerprint.infra.scanner.domain.ota.DownloadableFirmwareVersion
import com.simprints.infra.logging.Simber
import javax.inject.Inject

/**
 * This class serves as the remote data provider of the Firmware versions.
 *
 * @property fingerprintFileDownloader  the file downloader responsible for downloading the updated firmware
 */
internal class FirmwareRemoteDataSource @Inject constructor(
    private val fingerprintFileDownloader: FingerprintFileDownloader,
) {
    /**
     * Downloads the firmware binary at the given URL
     */
    suspend fun downloadFirmware(firmwareVersion: DownloadableFirmwareVersion): ByteArray {
        val fileUrl = fingerprintFileDownloader.getFileUrl(firmwareVersion.toStringForApi())
        Simber.d("Downloading firmware file at %s", fileUrl)
        return fingerprintFileDownloader.download(fileUrl)
    }
}
