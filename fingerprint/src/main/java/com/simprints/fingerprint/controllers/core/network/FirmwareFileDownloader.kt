package com.simprints.fingerprint.controllers.core.network

import com.simprints.fingerprint.scanner.data.FirmwareFileManager
import com.simprints.fingerprint.scanner.domain.versions.ChipFirmwareVersion

class FirmwareFileDownloader(private val fingerprintApiClientFactory: FingerprintApiClientFactory,
                             private val fingerprintFileDownloader: FingerprintFileDownloader,
                             private val firmwareFileManager: FirmwareFileManager) {

    suspend fun download() {
        val downloadableFirmwares = getDownloadableFirmwares()

        val savedVersions = firmwareFileManager.getAvailableScannerFirmwareVersions()

        val cypressToDownload = downloadableFirmwares.getFirmwareToDownloadOrNull(Chip.CYPRESS, savedVersions.cypress)
        val stmToDownload = downloadableFirmwares.getFirmwareToDownloadOrNull(Chip.STM, savedVersions.stm)
        val un20ToDownload = downloadableFirmwares.getFirmwareToDownloadOrNull(Chip.UN20, savedVersions.un20)

        cypressToDownload?.downloadAndSave()
        stmToDownload?.downloadAndSave()
        un20ToDownload?.downloadAndSave()
    }

    private suspend fun getDownloadableFirmwares(): List<DownloadableFirmwareVersion> {
        val fingerprintApiClient = fingerprintApiClientFactory.buildClient(FirmwareRemoteInterface::class)
        return fingerprintApiClient.executeCall("downloadFirmware") { api ->
            api.getAvailableDownloadableVersions().map { it.toDomain() }
        }
    }

    private fun List<DownloadableFirmwareVersion>.getFirmwareToDownloadOrNull(chip: Chip, savedVersion: ChipFirmwareVersion?): DownloadableFirmwareVersion? {
        val downloadableVersion = this.find { it.chip == chip } ?: return null
        if (savedVersion == null || savedVersion == ChipFirmwareVersion.UNKNOWN) return downloadableVersion
        return if (downloadableVersion.version > savedVersion) downloadableVersion else null
    }

    private suspend fun DownloadableFirmwareVersion.downloadAndSave() {
        val firmwareBytes = fingerprintFileDownloader.download(this.downloadUrl)
        when (chip) {
            Chip.CYPRESS -> firmwareFileManager.saveCypressFirmwareBytes(version, firmwareBytes)
            Chip.STM -> firmwareFileManager.saveStmFirmwareBytes(version, firmwareBytes)
            Chip.UN20 -> firmwareFileManager.saveUn20FirmwareBytes(version, firmwareBytes)
        }
    }
}
