package com.simprints.fingerprint.infra.scanner.domain.versions

import com.simprints.fingerprint.infra.scanner.domain.ota.DownloadableFirmwareVersion
import com.simprints.fingerprint.infra.scanner.domain.ota.DownloadableFirmwareVersion.Chip
import com.simprints.infra.config.store.models.Vero2Configuration

internal fun Vero2Configuration.Vero2FirmwareVersions.getMissingVersionsToDownload(
    savedFirmwareVersions: Map<Chip, Set<String>>,
): List<DownloadableFirmwareVersion> {
    val firmwareVersionsToDownload = ArrayList<DownloadableFirmwareVersion>()

    addAvailableCypressVersions(
        savedFirmwareVersions,
        this,
        firmwareVersionsToDownload,
    )
    addAvailableSTMVersions(
        savedFirmwareVersions,
        this,
        firmwareVersionsToDownload,
    )
    addAvailableUN20Versions(
        savedFirmwareVersions,
        this,
        firmwareVersionsToDownload,
    )

    return firmwareVersionsToDownload
}

private fun addAvailableCypressVersions(
    savedFirmwareVersions: Map<Chip, Set<String>>,
    configuredFirmwareVersions: Vero2Configuration.Vero2FirmwareVersions,
    firmwareVersionsToDownload: ArrayList<DownloadableFirmwareVersion>,
) {
    val localCypressVersions = savedFirmwareVersions[Chip.CYPRESS]
    val hasNotDownloadedCypressVersion = localCypressVersions == null ||
        !localCypressVersions.contains(configuredFirmwareVersions.cypress)

    if (hasNotDownloadedCypressVersion) {
        firmwareVersionsToDownload.add(
            DownloadableFirmwareVersion(Chip.CYPRESS, configuredFirmwareVersions.cypress),
        )
    }
}

private fun addAvailableSTMVersions(
    savedFirmwareVersions: Map<Chip, Set<String>>,
    configuredFirmwareVersions: Vero2Configuration.Vero2FirmwareVersions,
    firmwareVersionsToDownload: ArrayList<DownloadableFirmwareVersion>,
) {
    val localSTMVersions = savedFirmwareVersions[Chip.STM]
    val hasNotDownloadedStmVersion = localSTMVersions == null ||
        !localSTMVersions.contains(configuredFirmwareVersions.stm)

    if (hasNotDownloadedStmVersion) {
        firmwareVersionsToDownload.add(
            DownloadableFirmwareVersion(Chip.STM, configuredFirmwareVersions.stm),
        )
    }
}

private fun addAvailableUN20Versions(
    savedFirmwareVersions: Map<Chip, Set<String>>,
    configuredFirmwareVersions: Vero2Configuration.Vero2FirmwareVersions,
    firmwareVersionsToDownload: ArrayList<DownloadableFirmwareVersion>,
) {
    val localUN20Versions = savedFirmwareVersions[Chip.UN20]
    val hasNotDownloadedUn20Version = localUN20Versions == null ||
        !localUN20Versions.contains(configuredFirmwareVersions.un20)

    if (hasNotDownloadedUn20Version) {
        firmwareVersionsToDownload.add(
            DownloadableFirmwareVersion(Chip.UN20, configuredFirmwareVersions.un20),
        )
    }
}
