package com.simprints.fingerprint.scanner.data

import com.simprints.fingerprint.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.scanner.data.remote.FirmwareRemoteDataSource
import com.simprints.fingerprint.scanner.domain.ota.DownloadableFirmwareVersion
import com.simprints.fingerprint.scanner.domain.ota.DownloadableFirmwareVersion.Chip
import com.simprints.fingerprint.scanner.domain.versions.ChipFirmwareVersion
import com.simprints.logging.Simber

class FirmwareRepository(private val firmwareRemoteDataSource: FirmwareRemoteDataSource,
                         private val firmwareLocalDataSource: FirmwareLocalDataSource) {

    suspend fun updateStoredFirmwareFilesWithLatest() {
        val savedVersions = firmwareLocalDataSource.getAvailableScannerFirmwareVersions()
        Simber.d("Saved firmware versions: $savedVersions")

        val downloadableFirmwares = firmwareRemoteDataSource.getDownloadableFirmwares(savedVersions)
        // issue with timber logging URLs when interpolated in kotlin, check out this article
        // https://proandroiddev.com/be-careful-what-you-log-it-could-crash-your-app-5fc67a44c842
        val versionString = downloadableFirmwares.joinToString()
        Simber.d("Firmwares available for download: %s", versionString)

        val cypressToDownload = downloadableFirmwares.getVersionToDownloadOrNull(Chip.CYPRESS, savedVersions.cypress)
        val stmToDownload = downloadableFirmwares.getVersionToDownloadOrNull(Chip.STM, savedVersions.stm)
        val un20ToDownload = downloadableFirmwares.getVersionToDownloadOrNull(Chip.UN20, savedVersions.un20)

        cypressToDownload?.downloadAndSave()
        stmToDownload?.downloadAndSave()
        un20ToDownload?.downloadAndSave()
    }

    private fun List<DownloadableFirmwareVersion>.getVersionToDownloadOrNull(chip: Chip, savedVersion: ChipFirmwareVersion?): DownloadableFirmwareVersion? {
        val downloadableVersion = this.find { it.chip == chip } ?: return null
        if (savedVersion == null || savedVersion == ChipFirmwareVersion.UNKNOWN) return downloadableVersion
        return if (downloadableVersion.version > savedVersion) downloadableVersion else null
    }

    private suspend fun DownloadableFirmwareVersion.downloadAndSave() {
        val firmwareBytes = firmwareRemoteDataSource.downloadFile(this.downloadUrl)
        when (chip) {
            Chip.CYPRESS -> firmwareLocalDataSource.saveCypressFirmwareBytes(version, firmwareBytes)
            Chip.STM -> firmwareLocalDataSource.saveStmFirmwareBytes(version, firmwareBytes)
            Chip.UN20 -> firmwareLocalDataSource.saveUn20FirmwareBytes(version, firmwareBytes)
        }
    }
}
