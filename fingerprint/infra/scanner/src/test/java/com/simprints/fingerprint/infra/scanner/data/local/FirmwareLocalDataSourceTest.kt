package com.simprints.fingerprint.infra.scanner.data.local

import android.content.Context
import com.google.common.truth.Truth
import com.simprints.core.tools.utils.FileUtil
import com.simprints.fingerprint.infra.scanner.data.local.FirmwareLocalDataSource.Companion.FIRMWARE_DIR
import com.simprints.fingerprint.infra.scanner.domain.ota.DownloadableFirmwareVersion.Chip
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

internal class FirmwareLocalDataSourceTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var context: Context

    @MockK
    lateinit var fileUtil: FileUtil

    @RelaxedMockK
    lateinit var rootDir: File

    @RelaxedMockK
    lateinit var cypressDir: File

    @RelaxedMockK
    lateinit var stmDir: File

    @RelaxedMockK
    lateinit var un20Dir: File

    lateinit var firmwareLocalDataSource: FirmwareLocalDataSource

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        firmwareLocalDataSource =
            FirmwareLocalDataSource(context, testCoroutineRule.testCoroutineDispatcher, fileUtil)

        every { context.filesDir } returns mockk()
        every { fileUtil.createFile(any<File>(), FIRMWARE_DIR) } returns rootDir
        every { fileUtil.createFile(any<File>(), "$FIRMWARE_DIR/cypress") } returns cypressDir
        every { fileUtil.createFile(any<File>(), "$FIRMWARE_DIR/stm") } returns stmDir
        every { fileUtil.createFile(any<File>(), "$FIRMWARE_DIR/un20") } returns un20Dir
    }

    @Test
    fun getAvailableScannerFirmwareVersions() = runTest {
        // Given
        every { cypressDir.listFiles() } returns null
        every { stmDir.listFiles() } returns arrayOf<File>(mockk(relaxed = true))
        every { un20Dir.listFiles() } returns arrayOf<File>(mockk(relaxed = true))
        // When
        val result = firmwareLocalDataSource.getAvailableScannerFirmwareVersions()
        // Then
        Truth.assertThat(result[Chip.CYPRESS]).isEmpty()
        Truth.assertThat(result[Chip.STM]).isNotEmpty()
        Truth.assertThat(result[Chip.UN20]).isNotEmpty()
    }

    @Test
    fun loadCypressFirmwareBytes() = runTest {
        // Given
        val firmwareVersion = "123"
        val mockkFirmwareFile = mockk<File>(relaxed = true)
        val firmwareBytes = byteArrayOf(0x1)
        every { mockkFirmwareFile.exists() } returns true
        every { fileUtil.readBytes(mockkFirmwareFile) } returns firmwareBytes
        every { fileUtil.createFile(cypressDir, firmwareVersion) } returns mockkFirmwareFile
        // when
        val result = firmwareLocalDataSource.loadCypressFirmwareBytes(firmwareVersion)
        // Then
        Truth.assertThat(result).isEqualTo(firmwareBytes)
    }

    @Test(expected = IllegalStateException::class)
    fun `loadCypressFirmwareBytes file not exist throws IllegalStateException`() = runTest {
        // Given
        val firmwareVersion = "123"
        val mockkFirmwareFile = mockk<File>(relaxed = true)
        every { mockkFirmwareFile.exists() } returns false
        every { fileUtil.createFile(cypressDir, firmwareVersion) } returns mockkFirmwareFile

        // when
        firmwareLocalDataSource.loadCypressFirmwareBytes(firmwareVersion)
        // Then throws
    }

    @Test
    fun loadStmFirmwareBytes() = runTest {
        // Given
        val firmwareVersion = "123"
        val mockkFirmwareFile = mockk<File>(relaxed = true)
        val firmwareBytes = byteArrayOf(0x1)
        every { mockkFirmwareFile.exists() } returns true
        every { fileUtil.readBytes(mockkFirmwareFile) } returns firmwareBytes
        every { fileUtil.createFile(stmDir, firmwareVersion) } returns mockkFirmwareFile
        // when
        val result = firmwareLocalDataSource.loadStmFirmwareBytes(firmwareVersion)
        // Then
        Truth.assertThat(result).isEqualTo(firmwareBytes)
    }

    @Test
    fun loadUn20FirmwareBytes() = runTest {
        // Given
        val firmwareVersion = "123"
        val mockkFirmwareFile = mockk<File>(relaxed = true)
        val firmwareBytes = byteArrayOf(0x1)
        every { mockkFirmwareFile.exists() } returns true
        every { fileUtil.readBytes(mockkFirmwareFile) } returns firmwareBytes
        every { fileUtil.createFile(un20Dir, firmwareVersion) } returns mockkFirmwareFile
        // when
        val result = firmwareLocalDataSource.loadUn20FirmwareBytes(firmwareVersion)
        // Then
        Truth.assertThat(result).isEqualTo(firmwareBytes)
    }

    @Test
    fun deleteCypressFirmware() = runTest {
        // Given
        val firmwareVersion = "123"
        val mockkFirmwareFile = mockk<File>(relaxed = true)
        every { fileUtil.createFile(cypressDir, firmwareVersion) } returns mockkFirmwareFile
        // When
        firmwareLocalDataSource.deleteCypressFirmware(firmwareVersion)
        // Then
        verify { mockkFirmwareFile.delete() }
    }

    @Test
    fun deleteUn20Firmware() = runTest {
        // Given
        val firmwareVersion = "123"
        val mockkFirmwareFile = mockk<File>(relaxed = true)
        every { fileUtil.createFile(un20Dir, firmwareVersion) } returns mockkFirmwareFile
        // When
        firmwareLocalDataSource.deleteUn20Firmware(firmwareVersion)
        // Then
        verify { mockkFirmwareFile.delete() }
    }

    @Test
    fun deleteStmFirmware() = runTest {
        // Given
        val firmwareVersion = "123"
        val mockkFirmwareFile = mockk<File>(relaxed = true)
        every { fileUtil.createFile(stmDir, firmwareVersion) } returns mockkFirmwareFile
        // When
        firmwareLocalDataSource.deleteStmFirmware(firmwareVersion)
        // Then
        verify { mockkFirmwareFile.delete() }
    }

    @Test
    fun saveCypressFirmwareBytes() = runTest {
        // Given
        val firmwareVersion = "123"
        val mockkFirmwareFile = mockk<File>(relaxed = true)
        val firmwareBytes = byteArrayOf(0x1)
        every { fileUtil.createFile(cypressDir, firmwareVersion) } returns mockkFirmwareFile
        every { fileUtil.writeBytes(mockkFirmwareFile, firmwareBytes) } just Runs

        // when
        firmwareLocalDataSource.saveCypressFirmwareBytes(firmwareVersion, firmwareBytes)
        // Then
        verify { fileUtil.writeBytes(mockkFirmwareFile, firmwareBytes) }
    }

    @Test
    fun saveStmFirmwareBytes() = runTest {
        // Given
        val firmwareVersion = "123"
        val mockkFirmwareFile = mockk<File>(relaxed = true)
        val firmwareBytes = byteArrayOf(0x1)
        every { fileUtil.createFile(stmDir, firmwareVersion) } returns mockkFirmwareFile
        every { fileUtil.writeBytes(mockkFirmwareFile, firmwareBytes) } just Runs

        // when
        firmwareLocalDataSource.saveStmFirmwareBytes(firmwareVersion, firmwareBytes)
        // Then
        verify { fileUtil.writeBytes(mockkFirmwareFile, firmwareBytes) }
    }

    @Test
    fun saveUn20FirmwareBytes() = runTest {
        // Given
        val firmwareVersion = "123"
        val mockkFirmwareFile = mockk<File>(relaxed = true)
        val firmwareBytes = byteArrayOf(0x1)
        every { fileUtil.createFile(un20Dir, firmwareVersion) } returns mockkFirmwareFile
        every { fileUtil.writeBytes(mockkFirmwareFile, firmwareBytes) } just Runs

        // when
        firmwareLocalDataSource.saveUn20FirmwareBytes(firmwareVersion, firmwareBytes)
        // Then
        verify { fileUtil.writeBytes(mockkFirmwareFile, firmwareBytes) }
    }

    @Test
    fun deleteAll() = runTest {
        firmwareLocalDataSource.deleteAllFirmware()

        verify { rootDir.deleteRecursively() }
    }
}
