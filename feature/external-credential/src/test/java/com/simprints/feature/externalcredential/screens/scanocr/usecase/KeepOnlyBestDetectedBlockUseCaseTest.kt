package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.google.common.truth.Truth.*
import com.simprints.feature.externalcredential.screens.scanocr.model.DetectedOcrBlock
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.infra.credential.store.CredentialImageRepository
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
    private lateinit var credentialImageRepository: CredentialImageRepository

    private lateinit var useCase: KeepOnlyBestDetectedBlockUseCase

    private val bestBlockImagePath = "/path/to/best/image.jpg"
    private val otherBlockImagePath1 = "/path/to/other1/image.jpg"
    private val otherBlockImagePath2 = "/path/to/other2/image.jpg"
    private val credentialNhis = "12345678"
    private val credentialGhanaId = "GHA-123456789-0"
    private val credentialLengthNhis = credentialNhis.length
    private val credentialLengthGhanaID = credentialGhanaId.length

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = KeepOnlyBestDetectedBlockUseCase(
            getExternalCredentialBasedOnConfidenceUseCase = getExternalCredentialBasedOnConfidenceUseCase,
            findBestTextBlockForCredentialUseCase = findBestTextBlockForCredentialUseCase,
            credentialImageRepository = credentialImageRepository,
        )
    }

    private fun createMockBlock(imagePath: String) = mockk<DetectedOcrBlock> {
        every { this@mockk.imagePath } returns imagePath
    }

    @Test
    fun `returns best block and deletes other cached images for NHIS card`() = runTest {
        val bestBlock = createMockBlock(bestBlockImagePath)
        val otherBlock1 = createMockBlock(otherBlockImagePath1)
        val otherBlock2 = createMockBlock(otherBlockImagePath2)
        val allBlocks = listOf(bestBlock, otherBlock1, otherBlock2)

        every { getExternalCredentialBasedOnConfidenceUseCase(allBlocks, credentialLengthNhis) } returns credentialNhis
        every { findBestTextBlockForCredentialUseCase(credentialNhis, allBlocks) } returns bestBlock

        val result = useCase(allBlocks, OcrDocumentType.NhisCard)

        assertThat(result).isEqualTo(bestBlock)
        coVerify { credentialImageRepository.deleteByPath(otherBlockImagePath1) }
        coVerify { credentialImageRepository.deleteByPath(otherBlockImagePath2) }
        coVerify(exactly = 0) { credentialImageRepository.deleteByPath(bestBlockImagePath) }
    }

    @Test
    fun `returns best block and deletes other cached images for Ghana ID card`() = runTest {
        val bestBlock = createMockBlock(bestBlockImagePath)
        val otherBlock1 = createMockBlock(otherBlockImagePath1)
        val otherBlock2 = createMockBlock(otherBlockImagePath2)
        val allBlocks = listOf(bestBlock, otherBlock1, otherBlock2)

        every { getExternalCredentialBasedOnConfidenceUseCase(allBlocks, credentialLengthGhanaID) } returns credentialGhanaId
        every { findBestTextBlockForCredentialUseCase(credentialGhanaId, allBlocks) } returns bestBlock

        val result = useCase(allBlocks, OcrDocumentType.GhanaIdCard)

        assertThat(result).isEqualTo(bestBlock)
        coVerify { credentialImageRepository.deleteByPath(otherBlockImagePath1) }
        coVerify { credentialImageRepository.deleteByPath(otherBlockImagePath2) }
        coVerify(exactly = 0) { credentialImageRepository.deleteByPath(bestBlockImagePath) }
    }

    @Test
    fun `uses correct credential length for NHIS card`() = runTest {
        val bestBlock = createMockBlock(bestBlockImagePath)
        val allBlocks = listOf(bestBlock)

        every { getExternalCredentialBasedOnConfidenceUseCase(allBlocks, credentialLengthNhis) } returns credentialNhis
        every { findBestTextBlockForCredentialUseCase(credentialNhis, allBlocks) } returns bestBlock

        useCase(allBlocks, OcrDocumentType.NhisCard)

        verify { getExternalCredentialBasedOnConfidenceUseCase(allBlocks, credentialLengthNhis) }
    }

    @Test
    fun `uses correct credential length for Ghana ID card`() = runTest {
        val bestBlock = createMockBlock(bestBlockImagePath)
        val allBlocks = listOf(bestBlock)

        every { getExternalCredentialBasedOnConfidenceUseCase(allBlocks, credentialLengthGhanaID) } returns credentialGhanaId
        every { findBestTextBlockForCredentialUseCase(credentialGhanaId, allBlocks) } returns bestBlock

        useCase(allBlocks, OcrDocumentType.GhanaIdCard)

        verify { getExternalCredentialBasedOnConfidenceUseCase(allBlocks, credentialLengthGhanaID) }
    }

    @Test
    fun `does not delete any images when only one block exists`() = runTest {
        val singleBlock = createMockBlock(bestBlockImagePath)
        val allBlocks = listOf(singleBlock)

        every { getExternalCredentialBasedOnConfidenceUseCase(allBlocks, credentialLengthNhis) } returns credentialNhis
        every { findBestTextBlockForCredentialUseCase(credentialNhis, allBlocks) } returns singleBlock

        val result = useCase(allBlocks, OcrDocumentType.NhisCard)

        assertThat(result).isEqualTo(singleBlock)
        coVerify(exactly = 0) { credentialImageRepository.deleteByPath(any()) }
    }

    @Test
    fun `passes credential to find best block use case`() = runTest {
        val bestBlock = createMockBlock(bestBlockImagePath)
        val allBlocks = listOf(bestBlock)

        every { getExternalCredentialBasedOnConfidenceUseCase(allBlocks, credentialLengthNhis) } returns credentialNhis
        every { findBestTextBlockForCredentialUseCase(credentialNhis, allBlocks) } returns bestBlock

        useCase(allBlocks, OcrDocumentType.NhisCard)

        verify { findBestTextBlockForCredentialUseCase(credentialNhis, allBlocks) }
    }

    @Test
    fun `deletes multiple other images but keeps best block image`() = runTest {
        val bestBlock = createMockBlock(bestBlockImagePath)
        val blocks = listOf(
            createMockBlock("/path1.jpg"),
            createMockBlock("/path2.jpg"),
            bestBlock,
            createMockBlock("/path3.jpg"),
            createMockBlock("/path4.jpg"),
        )
        val mockCredential = "GHA-123456789-0"

        every { getExternalCredentialBasedOnConfidenceUseCase(blocks, credentialLengthGhanaID) } returns mockCredential
        every { findBestTextBlockForCredentialUseCase(mockCredential, blocks) } returns bestBlock

        useCase(blocks, OcrDocumentType.GhanaIdCard)

        coVerify { credentialImageRepository.deleteByPath("/path1.jpg") }
        coVerify { credentialImageRepository.deleteByPath("/path2.jpg") }
        coVerify { credentialImageRepository.deleteByPath("/path3.jpg") }
        coVerify { credentialImageRepository.deleteByPath("/path4.jpg") }
        coVerify(exactly = 0) { credentialImageRepository.deleteByPath(bestBlockImagePath) }
    }
}
