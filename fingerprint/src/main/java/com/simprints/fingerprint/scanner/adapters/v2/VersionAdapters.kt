package com.simprints.fingerprint.scanner.adapters.v2

import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.scanner.domain.versions.*
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20AppVersion
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.StmFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.root.models.CypressFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.root.models.UnifiedVersionInformation

fun UnifiedVersionInformation.toScannerVersion() =
    ScannerVersion(
        ScannerGeneration.VERO_2,
        toScannerFirmwareVersions(),
        toScannerApiVersions()
    )

fun UnifiedVersionInformation.toScannerFirmwareVersions() =
    ScannerFirmwareVersions(
        cypress = cypressFirmwareVersion.toChipFirmwareVersion(),
        stm = stmFirmwareVersion.toChipFirmwareVersion(),
        un20 = un20AppVersion.toChipFirmwareVersion()
    )

fun UnifiedVersionInformation.toScannerApiVersions() =
    ScannerApiVersions(
        cypress = cypressFirmwareVersion.toChipApiVersion(),
        stm = stmFirmwareVersion.toChipApiVersion(),
        un20 = un20AppVersion.toChipApiVersion()
    )

fun CypressFirmwareVersion.toChipFirmwareVersion() = ChipFirmwareVersion(firmwareMajorVersion.toInt(), firmwareMinorVersion.toInt())
fun StmFirmwareVersion.toChipFirmwareVersion() = ChipFirmwareVersion(firmwareMajorVersion.toInt(), firmwareMinorVersion.toInt())
fun Un20AppVersion.toChipFirmwareVersion() = ChipFirmwareVersion(firmwareMajorVersion.toInt(), firmwareMinorVersion.toInt())

fun CypressFirmwareVersion.toChipApiVersion() = ChipApiVersion(apiMajorVersion.toInt(), apiMinorVersion.toInt())
fun StmFirmwareVersion.toChipApiVersion() = ChipApiVersion(apiMajorVersion.toInt(), apiMinorVersion.toInt())
fun Un20AppVersion.toChipApiVersion() = ChipApiVersion(apiMajorVersion.toInt(), apiMinorVersion.toInt())

/** @throws IllegalArgumentException if any of the versions are UNKNOWN */
fun ScannerVersion.toUnifiedVersionInformation() =
    UnifiedVersionInformation(
        computeMasterVersion(),
        cypressFirmwareVersion = CypressFirmwareVersion(
            apiMajorVersion = api.cypress.major.toShort(),
            apiMinorVersion = api.cypress.minor.toShort(),
            firmwareMajorVersion = firmware.cypress.major.toShort(),
            firmwareMinorVersion = firmware.cypress.minor.toShort()
        ),
        stmFirmwareVersion = StmFirmwareVersion(
            apiMajorVersion = api.stm.major.toShort(),
            apiMinorVersion = api.stm.minor.toShort(),
            firmwareMajorVersion = firmware.stm.major.toShort(),
            firmwareMinorVersion = firmware.stm.minor.toShort()
        ),
        un20AppVersion = Un20AppVersion(
            apiMajorVersion = api.un20.major.toShort(),
            apiMinorVersion = api.un20.minor.toShort(),
            firmwareMajorVersion = firmware.un20.major.toShort(),
            firmwareMinorVersion = firmware.un20.minor.toShort()
        )
    )
