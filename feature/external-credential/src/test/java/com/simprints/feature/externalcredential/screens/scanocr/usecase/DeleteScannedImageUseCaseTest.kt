package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.google.common.truth.Truth.*
import com.simprints.testtools.common.syntax.assertThrows
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeleteScannedImageUseCaseTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var useCase: DeleteScannedImageUseCase

    @Before
    fun setUp() {
        useCase = DeleteScannedImageUseCase(testDispatcher)
    }

    @Test
    fun `deletes file when it exists`() = runTest(testDispatcher) {
        val tempFile = kotlin.io.path
            .createTempFile()
            .toFile()
        tempFile.writeText("any-content")
        check(tempFile.exists())
        useCase(tempFile.absolutePath)
        assertThat(tempFile.exists()).isFalse()
    }

    @Test
    fun `throws when file does not exist`() = runTest(testDispatcher) {
        val nonExistentPath = "file-doesnt-exist.png"
        assertThrows<IllegalArgumentException> {
            useCase(nonExistentPath)
        }
    }
}
