package com.simprints.fingerprint.scanner.data

import android.content.Context
import android.os.Environment
import com.simprints.fingerprint.scanner.domain.versions.ChipFirmwareVersion
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import java.io.File

class FirmwareFileManager(private val context: Context) {

    fun getAvailableScannerFirmwareVersions(): ScannerFirmwareVersions? = ScannerFirmwareVersions( // TODO : temporary for testing
        cypress = ChipFirmwareVersion(1, 2),
        stm = ChipFirmwareVersion.UNKNOWN,
        un20 = ChipFirmwareVersion.UNKNOWN
    )

    fun getCypressFirmwareBytes(): ByteArray { // TODO : temporary for tetsing
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "cypress_firmware_app_1-2_api_1-1.bin")
        return file.readBytes()
    }

    fun getStmFirmwareBytes(): ByteArray = byteArrayOf() // TODO : temporary for testing

    fun getUn20FirmwareBytes(): ByteArray = byteArrayOf() // TODO : temporary for testing
}
