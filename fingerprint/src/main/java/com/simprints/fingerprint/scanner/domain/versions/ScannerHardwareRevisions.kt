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
        localFirmwareVersions: ScannerFirmwareVersions
    ): List<DownloadableFirmwareVersion> {
        val result = ArrayList<DownloadableFirmwareVersion>()
        val firmwareVersions = get(hardwareVersion) ?: return result

        if (firmwareVersions.cypress != localFirmwareVersions.cypress) {
            result.add(
                DownloadableFirmwareVersion(
                    DownloadableFirmwareVersion.Chip.CYPRESS,
                    firmwareVersions.cypress
                )
            )
        }
        if (firmwareVersions.stm != localFirmwareVersions.stm) {
            result.add(
                DownloadableFirmwareVersion(
                    DownloadableFirmwareVersion.Chip.STM,
                    firmwareVersions.stm
                )
            )
        }
        if (firmwareVersions.un20 != localFirmwareVersions.un20) {
            result.add(
                DownloadableFirmwareVersion(
                    DownloadableFirmwareVersion.Chip.UN20,
                    firmwareVersions.un20
                )
            )
        }
        return result
    }
}
