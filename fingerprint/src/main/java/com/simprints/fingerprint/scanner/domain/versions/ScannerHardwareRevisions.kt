package com.simprints.fingerprint.scanner.domain.versions

import com.simprints.fingerprint.scanner.domain.ota.DownloadableFirmwareVersion


class ScannerHardwareRevisions : HashMap<String, ScannerFirmwareVersions>() {
    /**
     * Allows for querying whether there are more up-to-date firmware versions by sending the currently saved versions
     *
     * @param hardwareVersion
     * @param localFirmwareVersions
     * @return
     */
    fun availableForDownload(
        hardwareVersion: String,
        localFirmwareVersions: Map<DownloadableFirmwareVersion.Chip, Set<String>>
    ): List<DownloadableFirmwareVersion> {
        val result = ArrayList<DownloadableFirmwareVersion>()
        val availableFirmwareVersions = get(hardwareVersion) ?: return result

        addAvailableCypressVersions(localFirmwareVersions, availableFirmwareVersions, result)
        addAvailableSTMVersions(localFirmwareVersions, availableFirmwareVersions, result)
        addAvailableUN20Versions(localFirmwareVersions, availableFirmwareVersions, result)

        return result
    }

    private fun addAvailableCypressVersions(
        localFirmwareVersions: Map<DownloadableFirmwareVersion.Chip, Set<String>>,
        availableFirmwareVersions: ScannerFirmwareVersions,
        result: ArrayList<DownloadableFirmwareVersion>
    ) {
        val localCypressVersions = localFirmwareVersions[DownloadableFirmwareVersion.Chip.CYPRESS]
        if (localCypressVersions == null || !localCypressVersions.contains(
                availableFirmwareVersions.cypress
            )
        ) {
            result.add(
                DownloadableFirmwareVersion(
                    DownloadableFirmwareVersion.Chip.CYPRESS,
                    availableFirmwareVersions.cypress
                )
            )
        }
    }

    private fun addAvailableSTMVersions(
        localFirmwareVersions: Map<DownloadableFirmwareVersion.Chip, Set<String>>,
        availableFirmwareVersions: ScannerFirmwareVersions,
        result: ArrayList<DownloadableFirmwareVersion>
    ) {
        val localSTMVersions = localFirmwareVersions[DownloadableFirmwareVersion.Chip.STM]

        if (localSTMVersions == null || !localSTMVersions.contains(
                availableFirmwareVersions.stm
            )
        ) {
            result.add(
                DownloadableFirmwareVersion(
                    DownloadableFirmwareVersion.Chip.STM,
                    availableFirmwareVersions.stm
                )
            )
        }
    }

    private fun addAvailableUN20Versions(
        localFirmwareVersions: Map<DownloadableFirmwareVersion.Chip, Set<String>>,
        availableFirmwareVersions: ScannerFirmwareVersions,
        result: ArrayList<DownloadableFirmwareVersion>
    ) {
        val localUN20Versions = localFirmwareVersions[DownloadableFirmwareVersion.Chip.UN20]
        if (localUN20Versions == null || !localUN20Versions.contains(
                availableFirmwareVersions.un20
            )
        ) {
            result.add(
                DownloadableFirmwareVersion(
                    DownloadableFirmwareVersion.Chip.UN20,
                    availableFirmwareVersions.un20
                )
            )
        }
    }
}
