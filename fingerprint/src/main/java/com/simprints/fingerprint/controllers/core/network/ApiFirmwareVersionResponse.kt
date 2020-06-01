package com.simprints.fingerprint.controllers.core.network

import androidx.annotation.Keep
import com.simprints.fingerprint.scanner.domain.versions.ChipFirmwareVersion

@Keep
data class ApiFirmwareVersionResponse(
    val chipType: String,
    val version: String,
    val versionURL: String
)

/** @throws IllegalArgumentException */
fun ApiFirmwareVersionResponse.toDomain() =
    DownloadableFirmwareVersion(
        Chip.values().find { it.chipName == chipType }
            ?: throw IllegalArgumentException("Unexpected chipType in ApiFirmwareVersionResponse: $chipType"),
        ChipFirmwareVersion.fromString(version),
        versionURL
    )
