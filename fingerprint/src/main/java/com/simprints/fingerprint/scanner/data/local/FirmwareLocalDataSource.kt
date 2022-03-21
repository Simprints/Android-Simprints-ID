package com.simprints.fingerprint.scanner.data.local

import android.content.Context
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.logging.Simber
import java.io.File

/**
 * Handles reading and writing to local firmware files on the phone.
 * Files are saved at the path /firmware/(hardware)/(chipName)/(chipVersion).bin
 */
class FirmwareLocalDataSource(private val context: Context) {

    fun getAvailableScannerFirmwareVersions(hardwareVersion: String): ScannerFirmwareVersions =
        ScannerFirmwareVersions(
            cypress = getFirmwareVersionsInDir(hardwareVersion, CYPRESS_DIR).maxOrNull() ?: ScannerFirmwareVersions.UNKNOWN_VERSION,
            stm = getFirmwareVersionsInDir(hardwareVersion, STM_DIR).maxOrNull() ?: ScannerFirmwareVersions.UNKNOWN_VERSION,
            un20 = getFirmwareVersionsInDir(hardwareVersion, UN20_DIR).maxOrNull() ?: ScannerFirmwareVersions.UNKNOWN_VERSION
        )

    fun loadCypressFirmwareBytes(hardware: String) = loadFirmwareBytes(hardware, CYPRESS_DIR)

    fun loadStmFirmwareBytes(hardware: String) = loadFirmwareBytes(hardware, STM_DIR)

    fun loadUn20FirmwareBytes(hardware: String) = loadFirmwareBytes(hardware, UN20_DIR)

    private fun loadFirmwareBytes(hardwareVersion: String, chipDirName: String): ByteArray {
        val version = getFirmwareVersionsInDir(hardwareVersion, chipDirName).maxOrNull()
            ?: throw IllegalStateException("No available firmware file in $FIRMWARE_DIR/$chipDirName/")
        return getFile( chipDirName, version).readBytes()
    }

    fun saveCypressFirmwareBytes( version: String, bytes: ByteArray) =
        saveFirmwareBytes( CYPRESS_DIR, version, bytes)

    fun saveStmFirmwareBytes( version: String, bytes: ByteArray) =
        saveFirmwareBytes( STM_DIR, version, bytes)

    fun saveUn20FirmwareBytes( version: String, bytes: ByteArray) =
        saveFirmwareBytes( UN20_DIR, version, bytes)

    private fun saveFirmwareBytes( chipDirName: String, version: String, bytes: ByteArray) {
        Simber.d("Saving firmware file of ${bytes.size} bytes at $FIRMWARE_DIR/${version.replace(".", "-")}.$BIN_FILE_EXTENSION")
        val existingVersions = getFirmwareVersionsInDir( "Ebi to remove this param",chipDirName)
        getFile( chipDirName, version).writeBytes(bytes)
        existingVersions.filter { it != version }.forEach { getFile( chipDirName, it).delete() }
    }
// Ebi to decide how to use hardware version
    private fun getFirmwareVersionsInDir(hardwareVersion: String, chipDirName: String): List<String> =
        getDir( chipDirName).listFiles { _, name ->
            FIRMWARE_FILE_NAME_REGEX.containsMatchIn(name)
        }?.mapNotNull {
            try {
                it.name.split('.')[0].replace("-", ".")
            } catch (e: Exception) {
                Simber.e(e, "Error encountered when parsing firmware file name")
                null
            }
        } ?: emptyList()

    private fun getDir( chipDirName: String): File =
        File(context.filesDir, "$FIRMWARE_DIR/$chipDirName").also { it.mkdirs() }

    private fun getFile( chipDirName: String, version: String): File =
        File(getDir( chipDirName), "${version.replace(".", "-")}.$BIN_FILE_EXTENSION")

    companion object {
        // Todo find fitting regex for new naming scheme
        val FIRMWARE_FILE_NAME_REGEX = """^[0-9]+-[0-9]+\.bin$""".toRegex() // e.g. "12-345.bin"

        const val FIRMWARE_DIR = "firmware"

        const val CYPRESS_DIR = "cypress"
        const val STM_DIR = "stm"
        const val UN20_DIR = "un20"

        const val BIN_FILE_EXTENSION = "bin"
    }
}
