package com.simprints.fingerprint.scanner.data.remote

import androidx.annotation.Keep
import com.simprints.fingerprint.scanner.domain.ota.DownloadableFirmwareVersion
import com.simprints.fingerprint.scanner.domain.versions.ChipFirmwareVersion

@Keep
data class ApiDownloadableFirmwareVersion(
    val chipType: String,
    val version: String,
    val versionURL: String
)

/** @throws IllegalArgumentException */
fun ApiDownloadableFirmwareVersion.toDomain() =
    DownloadableFirmwareVersion(
        DownloadableFirmwareVersion.Chip.values().find { it.chipName == chipType }
            ?: throw IllegalArgumentException("Unexpected chipType in ApiFirmwareVersionResponse: $chipType"),
        ChipFirmwareVersion.fromString(version),
        versionURL
    )
