package com.simprints.fingerprint.scanner.domain.versions

import com.simprints.fingerprint.scanner.domain.ota.DownloadableFirmwareVersion
import com.simprints.fingerprint.scanner.domain.ota.DownloadableFirmwareVersion.Chip


class ScannerHardwareRevisions: HashMap<String, ScannerFirmwareVersions>() {
    /**
     * Allows for querying whether there are more up-to-date firmware versions by sending the currently saved versions
     *
     * @param hardwareVersion
     * @param localFirmwareVersions
     * @return
     */
    fun getAvailableVersionsForDownload(
        hardwareVersion: String,
        localFirmwareVersions: Map<Chip, Set<String>>
    ): List<DownloadableFirmwareVersion> {
        val availableFirmwareVersionsForDownload = ArrayList<DownloadableFirmwareVersion>()
        val hardwareFirmwareVersionsToDownload =
            get(hardwareVersion) ?: return availableFirmwareVersionsForDownload

        addAvailableCypressVersions(
            localFirmwareVersions,
            hardwareFirmwareVersionsToDownload,
            availableFirmwareVersionsForDownload
        )
        addAvailableSTMVersions(
            localFirmwareVersions,
            hardwareFirmwareVersionsToDownload,
            availableFirmwareVersionsForDownload
        )
        addAvailableUN20Versions(
            localFirmwareVersions,
            hardwareFirmwareVersionsToDownload,
            availableFirmwareVersionsForDownload
        )

        return availableFirmwareVersionsForDownload
    }

    private fun addAvailableCypressVersions(
        localFirmwareVersions: Map<Chip, Set<String>>,
        hardwareFirmwareVersionsToDownload: ScannerFirmwareVersions,
        availableFirmwareVersionsForDownload: ArrayList<DownloadableFirmwareVersion>
    ) {
        val localCypressVersions = localFirmwareVersions[Chip.CYPRESS]
        val hasNotDownloadedCypressVersion = localCypressVersions == null
            || !localCypressVersions.contains(hardwareFirmwareVersionsToDownload.cypress)

        if (hasNotDownloadedCypressVersion) {
            availableFirmwareVersionsForDownload.add(
                DownloadableFirmwareVersion(Chip.CYPRESS, hardwareFirmwareVersionsToDownload.cypress)
            )
        }
    }

    private fun addAvailableSTMVersions(
        localFirmwareVersions: Map<Chip, Set<String>>,
        hardwareFirmwareVersionsToDownload: ScannerFirmwareVersions,
        availableFirmwareVersionsForDownload: ArrayList<DownloadableFirmwareVersion>
    ) {
        val localSTMVersions = localFirmwareVersions[Chip.STM]
        val hasNotDownloadedStmVersion = localSTMVersions == null
            || !localSTMVersions.contains(hardwareFirmwareVersionsToDownload.stm)

        if (hasNotDownloadedStmVersion) {
            availableFirmwareVersionsForDownload.add(
                DownloadableFirmwareVersion(Chip.STM, hardwareFirmwareVersionsToDownload.stm)
            )
        }
    }

    private fun addAvailableUN20Versions(
        localFirmwareVersions: Map<Chip, Set<String>>,
        hardwareFirmwareVersionsToDownload: ScannerFirmwareVersions,
        availableFirmwareVersionsForDownload: ArrayList<DownloadableFirmwareVersion>
    ) {
        val localUN20Versions = localFirmwareVersions[Chip.UN20]
        val hasNotDownloadedUn20Version = localUN20Versions == null
            || !localUN20Versions.contains(hardwareFirmwareVersionsToDownload.un20)

        if (hasNotDownloadedUn20Version) {
            availableFirmwareVersionsForDownload.add(
                DownloadableFirmwareVersion(Chip.UN20, hardwareFirmwareVersionsToDownload.un20)
            )
        }
    }
}
