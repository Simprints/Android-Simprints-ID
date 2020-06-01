package com.simprints.fingerprint.controllers.core.network

class FirmwareFileDownloader(private val fingerprintApiClientFactory: FingerprintApiClientFactory) {

    suspend fun download() {
        val fingerprintApiClient = fingerprintApiClientFactory.buildClient(FirmwareRemoteInterface::class)
        fingerprintApiClient.executeCall("downloadFirmware") {
            val response = it.getAvailableDownloadableVersions()

        }
    }
}
