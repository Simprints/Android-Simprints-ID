package com.simprints.infra.credential.store

import android.content.Context
import android.graphics.Bitmap
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.credential.store.model.CredentialScanImageType
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

internal class CredentialImageRepositoryTest {
    @MockK
    lateinit var context: Context

    @MockK
    lateinit var bitmap: Bitmap

    private lateinit var cacheDir: File
    private lateinit var repository: CredentialImageRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        cacheDir = File("cache")
        every { context.cacheDir } returns cacheDir

        repository = CredentialImageRepository(
            context = context,
            ioDispatcher = testDispatcher,
        )
    }

    @After
    fun tearDown() {
        cacheDir.deleteRecursively()
    }

    @Test
    fun `saveCredentialScan saves bitmap for all types`() = runTest(testDispatcher) {
        CredentialScanImageType.entries.forEach { imageType ->
            every { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, any<FileOutputStream>()) } returns true
            cacheDir.mkdirs()

            val filePath = repository.saveCredentialScan(bitmap, imageType)

            assertThat(filePath).startsWith(cacheDir.absolutePath)
            assertThat(filePath).contains("credential_$imageType")
            assertThat(filePath).endsWith(".jpg")
            assertThat(File(filePath).exists()).isTrue()
            verify { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, any<FileOutputStream>()) }
        }
    }

    @Test
    fun `deleteByPath successfully deletes existing file`() {
        cacheDir.mkdirs()
        val testFile = File(cacheDir, "test_credential.jpg")
        testFile.createNewFile()
        assertThat(testFile.exists()).isTrue()
        repository.deleteByPath(testFile.absolutePath)
        assertThat(testFile.exists()).isFalse()
    }

    @Test
    fun `deleteByPath handles non-existing file without exceptions`() {
        val nonExistentPath = File(cacheDir, "non_existent.jpg").absolutePath
        repository.deleteByPath(nonExistentPath)
    }

    @Test
    fun `deleteAllCredentialScans deletes all credential scans`() {
        cacheDir.mkdirs()
        listOf("randomFile1", "randomFile2", "randomFile3").forEach { fileName ->
            File(cacheDir, fileName).createNewFile()
        }
        repository.deleteAllCredentialScans()
    }
}
