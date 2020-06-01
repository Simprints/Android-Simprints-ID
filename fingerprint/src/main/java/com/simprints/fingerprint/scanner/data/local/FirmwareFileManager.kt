package com.simprints.fingerprint.scanner.data.local

import android.content.Context
import com.simprints.fingerprint.scanner.domain.versions.ChipFirmwareVersion
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import timber.log.Timber
import java.io.File

class FirmwareFileManager(private val context: Context) {

    fun getAvailableScannerFirmwareVersions(): ScannerFirmwareVersions =
        ScannerFirmwareVersions(
            cypress = getFirmwareVersionsInDir(CYPRESS_DIR).max() ?: ChipFirmwareVersion.UNKNOWN,
            stm = getFirmwareVersionsInDir(STM_DIR).max() ?: ChipFirmwareVersion.UNKNOWN,
            un20 = getFirmwareVersionsInDir(UN20_DIR).max() ?: ChipFirmwareVersion.UNKNOWN
        )

    fun loadCypressFirmwareBytes() = loadFirmwareBytes(CYPRESS_DIR)

    fun loadStmFirmwareBytes() = loadFirmwareBytes(STM_DIR)

    fun loadUn20FirmwareBytes() = loadFirmwareBytes(UN20_DIR)

    private fun loadFirmwareBytes(chipDirName: String): ByteArray {
        val version = getFirmwareVersionsInDir(chipDirName).max()
            ?: throw IllegalStateException("No available firmware file in $FIRMWARE_DIR/$chipDirName/")
        return getFile(chipDirName, version).readBytes()
    }

    fun saveCypressFirmwareBytes(version: ChipFirmwareVersion, bytes: ByteArray) =
        saveFirmwareBytes(CYPRESS_DIR, version, bytes)

    fun saveStmFirmwareBytes(version: ChipFirmwareVersion, bytes: ByteArray) =
        saveFirmwareBytes(STM_DIR, version, bytes)

    fun saveUn20FirmwareBytes(version: ChipFirmwareVersion, bytes: ByteArray) =
        saveFirmwareBytes(UN20_DIR, version, bytes)

    private fun saveFirmwareBytes(chipDirName: String, version: ChipFirmwareVersion, bytes: ByteArray) {
        Timber.d("Saving firmware file of ${bytes.size} bytes at $FIRMWARE_DIR/${version.toString().replace(".", "-")}.$BIN_FILE_EXTENSION")
        val existingVersions = getFirmwareVersionsInDir(chipDirName)
        getFile(chipDirName, version).writeBytes(bytes)
        existingVersions.filter { it != version }.forEach { getFile(chipDirName, it).delete() }
    }

    private fun getFirmwareVersionsInDir(chipDirName: String): List<ChipFirmwareVersion> =
        getDir(chipDirName).listFiles { _, name ->
            FILE_NAME_REGEX.containsMatchIn(name)
        }?.mapNotNull {
            try {
                val majorMinor = it.name.split('.')[0].split('-')
                ChipFirmwareVersion(majorMinor[0].toInt(), majorMinor[1].toInt())
            } catch (e: Exception) {
                null
            }
        } ?: emptyList()

    private fun getDir(chipDirName: String): File =
        File(context.filesDir, "$FIRMWARE_DIR/$chipDirName").also { it.mkdirs() }

    private fun getFile(chipDirName: String, version: ChipFirmwareVersion): File =
        File(getDir(chipDirName), "${version.toString().replace(".", "-")}.$BIN_FILE_EXTENSION")

    companion object {
        val FILE_NAME_REGEX = """^[0-9]+-[0-9]+\.bin$""".toRegex() // e.g. "12-345.bin"

        const val FIRMWARE_DIR = "firmware"

        const val CYPRESS_DIR = "cypress"
        const val STM_DIR = "stm"
        const val UN20_DIR = "un20"

        const val BIN_FILE_EXTENSION = "bin"
    }
}
