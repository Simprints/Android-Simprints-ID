package com.simprints.fingerprint.scanner.data

import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions

class FirmwareFileManager {

    fun getAvailableScannerFirmwareVersions(): ScannerFirmwareVersions? = null

    fun getCypressFirmwareBytes(): ByteArray = byteArrayOf()

    fun getStmFirmwareBytes(): ByteArray = byteArrayOf()

    fun getUn20FirmwareBytes(): ByteArray = byteArrayOf()
}
