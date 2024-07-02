package com.simprints.fingerprint.infra.scanner.data

import com.simprints.fingerprint.infra.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.infra.scanner.data.remote.FirmwareRemoteDataSource
import com.simprints.fingerprint.infra.scanner.domain.ota.DownloadableFirmwareVersion
import com.simprints.fingerprint.infra.scanner.domain.ota.DownloadableFirmwareVersion.Chip
import com.simprints.fingerprint.infra.scanner.domain.versions.getMissingVersionsToDownload
import com.simprints.infra.config.store.models.Vero2Configuration.Vero2FirmwareVersions
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.logging.Simber
import javax.inject.Inject


/**
 * This class represents the firmware repository combining both local and remote data sources of
 * the firmware versions.
 */
class FirmwareRepository @Inject internal constructor(
    private val firmwareRemoteDataSource: FirmwareRemoteDataSource,
    private val firmwareLocalDataSource: FirmwareLocalDataSource,
    private val configManager: ConfigManager,
) {

    /**
     * This method is responsible for updating the firmware versions stored locally on the phone. It
     * first checks the local version and matches that against the remote versions, then subsequently updating the local versions that need to be updated.
     */
    suspend fun updateStoredFirmwareFilesWithLatest() {
        configManager.getProjectConfiguration().fingerprint?.secugenSimMatcher?.vero2?.firmwareVersions?.let {
            updateStoredFirmwareFilesWithLatest(it)
        }
        configManager.getProjectConfiguration().fingerprint?.nec?.vero2?.firmwareVersions?.let {
            updateStoredFirmwareFilesWithLatest(it)
        }
    }

    private suspend fun updateStoredFirmwareFilesWithLatest(firmwareConfiguration: Map<String, Vero2FirmwareVersions>) {
        firmwareConfiguration.forEach { (hardwareRevision, firmwareVersions) ->
            updateStoredFirmwareFilesWithLatest(firmwareVersions)
        }
    }

    private suspend fun updateStoredFirmwareFilesWithLatest(firmwareVersions: Vero2FirmwareVersions) {
        val savedVersions =  firmwareLocalDataSource.getAvailableScannerFirmwareVersions()
        Simber.d("Saved firmware versions: $savedVersions")

        val downloadableFirmwares = firmwareVersions.getMissingVersionsToDownload(savedVersions)

        // issue with timber logging URLs when interpolated in kotlin, check out this article
        // https://proandroiddev.com/be-careful-what-you-log-it-could-crash-your-app-5fc67a44c842
        val versionString = downloadableFirmwares.joinToString()
        Simber.d("Firmwares available for download: %s", versionString)

        val cypressToDownload = downloadableFirmwares.getVersionToDownloadOrNull(Chip.CYPRESS)
        val stmToDownload = downloadableFirmwares.getVersionToDownloadOrNull(Chip.STM)
        val un20ToDownload = downloadableFirmwares.getVersionToDownloadOrNull(Chip.UN20)

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
    suspend fun cleanUpOldFirmwareFiles() {
        Simber.d("Starting local Firmware files cleanup")

        val locallySavedFiles = firmwareLocalDataSource.getAvailableScannerFirmwareVersions()
        val cypressOfficialVersions = mutableSetOf<String>()
        val stmOfficialVersions = mutableSetOf<String>()
        val un20OfficialVersions = mutableSetOf<String>()

        val secuGenFirmwareVersions = configManager.getProjectConfiguration().fingerprint?.secugenSimMatcher?.vero2?.firmwareVersions?.entries
        val necFirmwareVersions = configManager.getProjectConfiguration().fingerprint?.nec?.vero2?.firmwareVersions?.entries
        (secuGenFirmwareVersions.orEmpty() + necFirmwareVersions.orEmpty()).forEach {
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

    private fun obsoleteItems(localVersions: Set<String>, officialVersions: Set<String>) =
        localVersions.filter { !officialVersions.contains(it) }

    suspend fun deleteAllFirmwareFiles() =  firmwareLocalDataSource.deleteAllFirmware()
}
