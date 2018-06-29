package com.simprints.id.data.prefs.sessionState.scannerAttributes


interface ScannerAttributesPreferencesManager {

    var macAddress: String
    var scannerId: String
    var hardwareVersion: Short
    val hardwareVersionString: String
        get() = if (hardwareVersion > -1) hardwareVersion.toString() else "unknown"

    fun resetScannerAttributes()

}
