package com.simprints.fingerprint.scanner.data

import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.scanner.data.remote.FirmwareRemoteDataSource
import com.simprints.fingerprint.scanner.domain.ota.DownloadableFirmwareVersion
import com.simprints.fingerprint.scanner.domain.ota.DownloadableFirmwareVersion.Chip
import com.simprints.logging.Simber


class FirmwareRepository(
    private val firmwareRemoteDataSource: FirmwareRemoteDataSource,
    private val firmwareLocalDataSource: FirmwareLocalDataSource,
    private val fingerprintPreferencesManager: FingerprintPreferencesManager
) {

    suspend fun updateStoredFirmwareFilesWithLatest() {
        fingerprintPreferencesManager.scannerHardwareRevisions.keys.forEach { hardwareVersion ->
            updateStoredFirmwareFilesWithLatest(hardwareVersion)
        }
    }

    private suspend fun updateStoredFirmwareFilesWithLatest(hardwareVersion: String) {
        val savedVersions =
            firmwareLocalDataSource.getAvailableScannerFirmwareVersions()
        Simber.d("Saved firmware versions: $savedVersions")

        val downloadableFirmwares = firmwareRemoteDataSource.getDownloadableFirmwares(
            hardwareVersion,
            savedVersions
        )
        // issue with timber logging URLs when interpolated in kotlin, check out this article
        // https://proandroiddev.com/be-careful-what-you-log-it-could-crash-your-app-5fc67a44c842
        val versionString = downloadableFirmwares.joinToString()
        Simber.d("Firmwares available for download: %s", versionString)

        val cypressToDownload =
            downloadableFirmwares.getVersionToDownloadOrNull(Chip.CYPRESS)
        val stmToDownload =
            downloadableFirmwares.getVersionToDownloadOrNull(Chip.STM)
        val un20ToDownload =
            downloadableFirmwares.getVersionToDownloadOrNull(Chip.UN20)

        cypressToDownload?.downloadAndSave()
        stmToDownload?.downloadAndSave()
        un20ToDownload?.downloadAndSave()
    }

    private fun List<DownloadableFirmwareVersion>.getVersionToDownloadOrNull(
        chip: Chip,
    ): DownloadableFirmwareVersion? {
        return this.find { it.chip == chip }
    }

    private suspend fun DownloadableFirmwareVersion.downloadAndSave() {
        val firmwareBytes = firmwareRemoteDataSource.downloadFirmware(this)
        when (chip) {
            Chip.CYPRESS -> firmwareLocalDataSource.saveCypressFirmwareBytes(version, firmwareBytes)
            Chip.STM -> firmwareLocalDataSource.saveStmFirmwareBytes(version, firmwareBytes)
            Chip.UN20 -> firmwareLocalDataSource.saveUn20FirmwareBytes(version, firmwareBytes)
        }
    }

    /**
     * Clean up old firmware files
     *
     */
     fun cleanUpOldFirmwareFiles() {
        Simber.d("Starting local Firmware files cleanup")

        val locallySavedFiles = firmwareLocalDataSource.getAvailableScannerFirmwareVersions()
        val cypressOfficialVersions = mutableSetOf<String>()
        val stmOfficialVersions = mutableSetOf<String>()
        val un20OfficialVersions = mutableSetOf<String>()
        fingerprintPreferencesManager.scannerHardwareRevisions.entries.forEach {
            cypressOfficialVersions.add(it.value.cypress)
            stmOfficialVersions.add(it.value.stm)
            un20OfficialVersions.add(it.value.un20)
        }
        locallySavedFiles.entries.forEach {
            when (it.key) {
                Chip.CYPRESS -> obsoleteItems(it.value, cypressOfficialVersions).forEach { firmwareFile ->
                    firmwareLocalDataSource.deleteCypressFirmware(firmwareFile)
                }
                Chip.STM -> obsoleteItems(it.value, stmOfficialVersions).forEach { firmwareFile ->
                    firmwareLocalDataSource.deleteStmFirmware(firmwareFile)
                }
                Chip.UN20 -> obsoleteItems(it.value, un20OfficialVersions).forEach { firmwareFile ->
                    firmwareLocalDataSource.deleteUn20Firmware(firmwareFile)
                }
            }
        }
    }

    private fun obsoleteItems(localVersions: Set<String>, officialVersions: Set<String>)=
        localVersions.filter { !officialVersions.contains(it) }

}
