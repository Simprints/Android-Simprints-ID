package com.simprints.id.data.prefs.sessionState.scannerAttributes

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.preferenceType.PrimitivePreference


class ScannerAttributesPreferencesManagerImpl(prefs: ImprovedSharedPreferences)
    : ScannerAttributesPreferencesManager {

    companion object {

        private const val MAC_ADDRESS_KEY = "MacAddress"
        private const val MAC_ADDRESS_DEFAULT = ""

        private const val HARDWARE_VERSION_KEY = "HardwareVersion"
        private const val HARDWARE_VERSION_DEFAULT: Short = -1

        private const val SCANNER_ID_KEY = "ScannerId"
        private const val SCANNER_ID_DEFAULT = ""

    }

    // Mac address of the scanner used for the current session
    override var macAddress: String
        by PrimitivePreference(prefs, MAC_ADDRESS_KEY, MAC_ADDRESS_DEFAULT)

    // Firmware version of the scanner used for the current session
    override var hardwareVersion: Short
        by PrimitivePreference(prefs, HARDWARE_VERSION_KEY, HARDWARE_VERSION_DEFAULT)

    // Unique identifier of the scanner used for the current session
    override var scannerId: String
        by PrimitivePreference(prefs, SCANNER_ID_KEY, SCANNER_ID_DEFAULT)

    override fun resetScannerAttributes() {
        macAddress = ScannerAttributesPreferencesManagerImpl.MAC_ADDRESS_DEFAULT
        hardwareVersion = ScannerAttributesPreferencesManagerImpl.HARDWARE_VERSION_DEFAULT
        scannerId = ScannerAttributesPreferencesManagerImpl.SCANNER_ID_DEFAULT
    }
}
