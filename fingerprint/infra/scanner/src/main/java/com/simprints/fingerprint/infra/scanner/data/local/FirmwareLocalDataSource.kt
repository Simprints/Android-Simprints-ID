package com.simprints.fingerprint.infra.scanner.data.local

import android.content.Context
import com.simprints.core.DispatcherIO
import com.simprints.core.tools.utils.FileUtil
import com.simprints.fingerprint.infra.scanner.domain.ota.DownloadableFirmwareVersion.Chip
import com.simprints.infra.logging.Simber
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import java.io.File
import javax.inject.Inject

/**
 * Handles reading and writing to local firmware files on the phone.
 * Files are saved at the path /firmware/(chipName)/(chipVersion)
 */
internal class FirmwareLocalDataSource(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher,
    private val fileUtil: FileUtil = FileUtil,
) {

    @Inject
    constructor(
        @ApplicationContext context: Context,
        @DispatcherIO dispatcher: CoroutineDispatcher,
    ) : this(context, dispatcher, FileUtil)

    fun getAvailableScannerFirmwareVersions() = runBlocking(dispatcher) {
        mapOf(
            Chip.CYPRESS to getFirmwareVersionsInDir(CYPRESS_DIR),
            Chip.STM to getFirmwareVersionsInDir(STM_DIR),
            Chip.UN20 to getFirmwareVersionsInDir(UN20_DIR)
        )
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

    fun loadCypressFirmwareBytes(chipVersion: String) = loadFirmwareBytes(chipVersion, CYPRESS_DIR)

    fun loadStmFirmwareBytes(chipVersion: String) = loadFirmwareBytes(chipVersion, STM_DIR)

    fun loadUn20FirmwareBytes(chipVersion: String) = loadFirmwareBytes(chipVersion, UN20_DIR)

    private fun loadFirmwareBytes(chipVersion: String, chipDirName: String): ByteArray = runBlocking(dispatcher) {
        val file = getFile(chipDirName, chipVersion)
        if (!file.exists()) throw IllegalStateException("$chipVersion firmware file is not available in $FIRMWARE_DIR/$chipDirName/")
        fileUtil.readBytes(file)
    }

    fun deleteCypressFirmware(chipVersion: String) = runBlocking(dispatcher) {
        getFile(CYPRESS_DIR, chipVersion).delete()
    }

    fun deleteStmFirmware(chipVersion: String) = runBlocking(dispatcher) {
        getFile(STM_DIR, chipVersion).delete()
    }

    fun deleteUn20Firmware(chipVersion: String) = runBlocking(dispatcher) {
        getFile(UN20_DIR, chipVersion).delete()
    }

    fun deleteAllFirmware() = runBlocking(dispatcher) {
        fileUtil.createFile(context.filesDir, FIRMWARE_DIR).deleteRecursively()
    }

    /**
     * This method is responsible for saving the provided Cypress firmware bytes, with the specified
     * version [ChipFirmwareVersion], to the cypress directory by delegating execution to the
     * [saveFirmwareBytes] method.
     *
     * @param version  the new [ChipFirmwareVersion] being saved
     * @param bytes   the firmware bytes to be saved to the file system
     *
     * @see [saveFirmwareBytes]
     */
    fun saveCypressFirmwareBytes(version: String, bytes: ByteArray) =
        saveFirmwareBytes(CYPRESS_DIR, version, bytes)

    /**
     * This method is responsible for saving the provided Stm firmware bytes, with the specified
     * version [ChipFirmwareVersion], to the Stm directory, by delegating execution to the
     * [saveFirmwareBytes] method.
     *
     * @param version  the new [ChipFirmwareVersion] being saved
     * @param bytes   the firmware bytes to be saved to the file system
     *
     * @see [saveFirmwareBytes]
     */
    fun saveStmFirmwareBytes(version: String, bytes: ByteArray) =
        saveFirmwareBytes(STM_DIR, version, bytes)

    /**
     * This method is responsible for saving the provided Un20 firmware bytes, with the specified
     * version [ChipFirmwareVersion], to the Un20 directory, by delegating execution to the
     * [saveFirmwareBytes] method.
     *
     * @param version  the new [ChipFirmwareVersion] being saved
     * @param bytes   the firmware bytes to be saved to the file system
     *
     * @see [saveFirmwareBytes]
     */
    fun saveUn20FirmwareBytes(version: String, bytes: ByteArray) =
        saveFirmwareBytes(UN20_DIR, version, bytes)

    /**
     * This method is responsible for saving the provided firmware bytes, to the specified
     * directory and deletes previously existing versions of files located within the same
     * directory.
     *
     * @param chipDirName  the name of the directory where the firmware binary will be saved
     * @param version  the new [ChipFirmwareVersion] being saved
     * @param bytes   the binary data to be saved to the file system
     */
    private fun saveFirmwareBytes(chipDirName: String, version: String, bytes: ByteArray) = runBlocking(dispatcher) {
        Simber.d("Saving firmware file of ${bytes.size} bytes at $FIRMWARE_DIR/$version")
        fileUtil.writeBytes(getFile(chipDirName, version), bytes)
    }

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
