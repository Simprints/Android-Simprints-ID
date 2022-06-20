package com.simprints.fingerprint.scanner.data.remote

import com.simprints.fingerprint.controllers.core.network.FingerprintApiClientFactory
import com.simprints.fingerprint.controllers.core.network.FingerprintFileDownloader
import com.simprints.fingerprint.scanner.domain.ota.DownloadableFirmwareVersion
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions

class FirmwareRemoteDataSource(private val fingerprintApiClientFactory: FingerprintApiClientFactory,
                               private val fingerprintFileDownloader: FingerprintFileDownloader) {

    /**
     * Allows for querying whether there are more up-to-date firmware versions by sending the currently saved versions
     */
    suspend fun getDownloadableFirmwares(savedVersion: ScannerFirmwareVersions): List<DownloadableFirmwareVersion> =
        fingerprintApiClientFactory.buildClient(FirmwareRemoteInterface::class)
            .executeCall("downloadFirmware") { api ->
                api.getAvailableDownloadableVersions(
                    aboveCypressVersion = savedVersion.cypress,
                    aboveStmVersion = savedVersion.stm,
                    aboveUn20Version = savedVersion.un20
                )
            }.values.map { it.toDomain() }

    /**
     * Downloads the firmware binary at the given URL
     */
    suspend fun downloadFile(url: String) = fingerprintFileDownloader.download(url)
}
