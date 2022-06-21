package com.simprints.fingerprint.scanner.data.local

import android.content.Context
import com.simprints.fingerprint.scanner.domain.ota.DownloadableFirmwareVersion.Chip
import com.simprints.id.tools.utils.FileUtil
import com.simprints.logging.Simber
import java.io.File

/**
 * Handles reading and writing to local firmware files on the phone.
 * Files are saved at the path /firmware/(chipName)/(chipVersion)
 */
class FirmwareLocalDataSource(private val context: Context,private val fileUtil: FileUtil = FileUtil) {

    fun getAvailableScannerFirmwareVersions() =
        mapOf(
            Chip.CYPRESS to getFirmwareVersionsInDir(CYPRESS_DIR),
            Chip.STM to getFirmwareVersionsInDir(STM_DIR),
            Chip.UN20 to getFirmwareVersionsInDir(UN20_DIR)
        )

    fun loadCypressFirmwareBytes(chipVersion: String) = loadFirmwareBytes(chipVersion, CYPRESS_DIR)

    fun loadStmFirmwareBytes(chipVersion: String) = loadFirmwareBytes(chipVersion, STM_DIR)

    fun loadUn20FirmwareBytes(chipVersion: String) = loadFirmwareBytes(chipVersion, UN20_DIR)

    fun deleteCypressFirmware(chipVersion: String) = getFile(CYPRESS_DIR, chipVersion).delete()

    fun deleteStmFirmware(chipVersion: String) = getFile(STM_DIR, chipVersion).delete()

    fun deleteUn20Firmware(chipVersion: String) = getFile(UN20_DIR, chipVersion).delete()

    private fun loadFirmwareBytes(chipVersion: String, chipDirName: String): ByteArray {
        val file = getFile(chipDirName, chipVersion)
        if (!file.exists()) throw IllegalStateException("$chipVersion firmware file is not available in $FIRMWARE_DIR/$chipDirName/")
        return fileUtil.readBytes(file)
    }

    fun saveCypressFirmwareBytes(version: String, bytes: ByteArray) =
        saveFirmwareBytes(CYPRESS_DIR, version, bytes)

    fun saveStmFirmwareBytes(version: String, bytes: ByteArray) =
        saveFirmwareBytes(STM_DIR, version, bytes)

    fun saveUn20FirmwareBytes(version: String, bytes: ByteArray) =
        saveFirmwareBytes(UN20_DIR, version, bytes)

    private fun saveFirmwareBytes(chipDirName: String, version: String, bytes: ByteArray) {
        Simber.d("Saving firmware file of ${bytes.size} bytes at $FIRMWARE_DIR/$version")
        fileUtil.writeBytes(getFile(chipDirName, version),bytes)
    }

    private fun getFirmwareVersionsInDir(chipDirName: String): Set<String> =
        (getDir(chipDirName).listFiles()?.mapNotNull {
            try {
                it.name
            } catch (e: Exception) {
                Simber.e(e, "Error encountered when parsing firmware file name")
                null
            }
        } ?: emptyList()).toSet()

    private fun getDir(chipDirName: String): File =
        fileUtil.createFile(context.filesDir, "$FIRMWARE_DIR/$chipDirName").also { it.mkdirs() }

    private fun getFile(chipDirName: String, version: String): File =
        fileUtil.createFile(getDir(chipDirName), version)

    companion object {
        const val FIRMWARE_DIR = "firmware"
        const val CYPRESS_DIR = "cypress"
        const val STM_DIR = "stm"
        const val UN20_DIR = "un20"
    }
}
