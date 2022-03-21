package com.simprints.fingerprint.scanner.data

import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.scanner.domain.versions.ScannerHardwareRevisions

object FirmwareTestData {
    internal const val HARDWARE_VERSION = "E-1"

    private const val CYPRESS_VERSION_HIGH = "1.E-1.1"

    private const val STM_VERSION_HIGH = "1.E-1.2"

    private const val UN20_VERSION_HIGH = "1.E-1.3"

    internal val scannerFirmwareVersions =
        ScannerFirmwareVersions(
            cypress = CYPRESS_VERSION_HIGH,
            stm = STM_VERSION_HIGH,
            un20 = UN20_VERSION_HIGH,
        )
    internal val RESPONSE_HARDWARE_REVISIONS_MAP = ScannerHardwareRevisions().apply {
        put(HARDWARE_VERSION, scannerFirmwareVersions)
    }

    internal const val SOME_URL = "some.url.com"
}


