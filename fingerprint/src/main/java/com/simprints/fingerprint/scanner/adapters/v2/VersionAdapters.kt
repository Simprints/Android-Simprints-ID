package com.simprints.fingerprint.scanner.adapters.v2

import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20ExtendedAppVersion
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.StmExtendedFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.root.models.CypressExtendedFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.root.models.ExtendedVersionInformation
import com.simprints.fingerprintscanner.v2.domain.root.models.ScannerInformation

fun ScannerInformation.toScannerVersion() = ScannerVersion(
    hardwareVersion = hardwareVersion,
    generation = ScannerGeneration.VERO_2,
    firmware = this.firmwareVersions.toScannerFirmwareVersions()
)

fun ExtendedVersionInformation.toScannerFirmwareVersions() =
    ScannerFirmwareVersions(
        cypress = cypressFirmwareVersion.versionAsString,
        stm = stmFirmwareVersion.versionAsString,
        un20 = un20AppVersion.versionAsString
    )

fun ScannerVersion.toExtendedVersionInformation() =
    ExtendedVersionInformation(
        cypressFirmwareVersion = CypressExtendedFirmwareVersion(firmware.cypress),
        stmFirmwareVersion = StmExtendedFirmwareVersion(firmware.stm),
        un20AppVersion = Un20ExtendedAppVersion(firmware.un20)
    )
