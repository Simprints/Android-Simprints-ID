package com.simprints.fingerprint.scanner.data.remote

import com.simprints.fingerprint.controllers.core.network.FingerprintApiClientFactory
import com.simprints.fingerprint.controllers.core.network.FingerprintFileDownloader
import com.simprints.fingerprint.scanner.domain.ota.DownloadableFirmwareVersion
import com.simprints.fingerprint.scanner.domain.versions.ChipFirmwareVersion
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions

class FirmwareRemoteDataSource(private val fingerprintApiClientFactory: FingerprintApiClientFactory,
                               private val fingerprintFileDownloader: FingerprintFileDownloader) {

    suspend fun getDownloadableFirmwares(savedVersion: ScannerFirmwareVersions): List<DownloadableFirmwareVersion> =
        fingerprintApiClientFactory.buildClient(FirmwareRemoteInterface::class)
            .executeCall("downloadFirmware") { api ->
                api.getAvailableDownloadableVersions(
                    aboveCypressVersion = savedVersion.cypress.toStringForApi(),
                    aboveStmVersion = savedVersion.stm.toStringForApi(),
                    aboveUn20Version = savedVersion.un20.toStringForApi()
                )
            }.values.map { it.toDomain() }

    private fun ChipFirmwareVersion.toStringForApi() =
        if (this == ChipFirmwareVersion.UNKNOWN) "0.0" else this.toString()

    suspend fun downloadFile(url: String) = fingerprintFileDownloader.download(url)
}
