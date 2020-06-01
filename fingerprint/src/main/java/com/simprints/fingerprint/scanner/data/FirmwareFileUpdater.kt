package com.simprints.fingerprint.scanner.data

import com.simprints.fingerprint.controllers.core.network.FingerprintApiClientFactory
import com.simprints.fingerprint.controllers.core.network.FingerprintFileDownloader
import com.simprints.fingerprint.scanner.data.remote.DownloadableFirmwareVersion.Chip
import com.simprints.fingerprint.scanner.data.local.FirmwareFileManager
import com.simprints.fingerprint.scanner.data.remote.DownloadableFirmwareVersion
import com.simprints.fingerprint.scanner.data.remote.FirmwareRemoteInterface
import com.simprints.fingerprint.scanner.data.remote.toDomain
import com.simprints.fingerprint.scanner.domain.versions.ChipFirmwareVersion
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import timber.log.Timber

class FirmwareFileUpdater(private val fingerprintApiClientFactory: FingerprintApiClientFactory,
                          private val fingerprintFileDownloader: FingerprintFileDownloader,
                          private val firmwareFileManager: FirmwareFileManager) {

    suspend fun download() {
        val savedVersions = firmwareFileManager.getAvailableScannerFirmwareVersions()
        Timber.d("Saved firmware versions: $savedVersions")

        val downloadableFirmwares = getDownloadableFirmwares(savedVersions)
        Timber.d("Firmwares available for download: $downloadableFirmwares")

        val cypressToDownload = downloadableFirmwares.getFirmwareToDownloadOrNull(Chip.CYPRESS, savedVersions.cypress)
        val stmToDownload = downloadableFirmwares.getFirmwareToDownloadOrNull(Chip.STM, savedVersions.stm)
        val un20ToDownload = downloadableFirmwares.getFirmwareToDownloadOrNull(Chip.UN20, savedVersions.un20)

        cypressToDownload?.downloadAndSave()
        stmToDownload?.downloadAndSave()
        un20ToDownload?.downloadAndSave()
    }

    private suspend fun getDownloadableFirmwares(savedVersion: ScannerFirmwareVersions): List<DownloadableFirmwareVersion> =
        fingerprintApiClientFactory.buildClient(FirmwareRemoteInterface::class)
            .executeCall("downloadFirmware") { api ->
                api.getAvailableDownloadableVersions(
                    aboveCypressVersion = savedVersion.cypress.toStringForApi(),
                    aboveStmVersion = savedVersion.stm.toStringForApi(),
                    aboveUn20Version = savedVersion.un20.toStringForApi()
                ).values.map { it.toDomain() }
            }

    private fun ChipFirmwareVersion.toStringForApi() =
        if (this == ChipFirmwareVersion.UNKNOWN) "0.0" else this.toString()

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
