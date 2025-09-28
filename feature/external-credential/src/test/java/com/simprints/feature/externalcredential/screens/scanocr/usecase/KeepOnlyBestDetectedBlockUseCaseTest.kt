package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.externalcredential.screens.scanocr.model.DetectedOcrBlock
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class KeepOnlyBestDetectedBlockUseCaseTest {

    @MockK
    private lateinit var getExternalCredentialBasedOnConfidenceUseCase: GetExternalCredentialBasedOnConfidenceUseCase

    @MockK
    private lateinit var findBestTextBlockForCredentialUseCase: FindBestTextBlockForCredentialUseCase

    @MockK
    private lateinit var deleteScannedImageUseCase: DeleteScannedImageUseCase

    private lateinit var useCase: KeepOnlyBestDetectedBlockUseCase

    private val bestBlockImagePath = "/path/to/best/image.jpg"
    private val otherBlockImagePath1 = "/path/to/other1/image.jpg"
    private val otherBlockImagePath2 = "/path/to/other2/image.jpg"

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = KeepOnlyBestDetectedBlockUseCase(
            getExternalCredentialBasedOnConfidenceUseCase = getExternalCredentialBasedOnConfidenceUseCase,
            findBestTextBlockForCredentialUseCase = findBestTextBlockForCredentialUseCase,
            deleteScannedImageUseCase = deleteScannedImageUseCase
        )
    }

    @Test
    fun `returns best block and deletes other cached images`() = runTest {
        val bestBlock = createMockBlock(bestBlockImagePath)
        val otherBlock1 = createMockBlock(otherBlockImagePath1)
        val otherBlock2 = createMockBlock(otherBlockImagePath2)
        val allBlocks = listOf(bestBlock, otherBlock1, otherBlock2)
        val mockCredential = "mockCredential"

        every { getExternalCredentialBasedOnConfidenceUseCase(allBlocks) } returns mockCredential
        every { findBestTextBlockForCredentialUseCase(mockCredential, allBlocks) } returns bestBlock

        val result = useCase(allBlocks)

        assertThat(result).isEqualTo(bestBlock)
        coVerify { deleteScannedImageUseCase(otherBlockImagePath1) }
        coVerify { deleteScannedImageUseCase(otherBlockImagePath2) }
        coVerify(exactly = 0) { deleteScannedImageUseCase(bestBlockImagePath) }
    }

    private fun createMockBlock(imagePath: String) = mockk<DetectedOcrBlock> {
        every { this@mockk.imagePath } returns imagePath
    }
}
