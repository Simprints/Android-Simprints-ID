package com.simprints.infra.images.remote.signedurl.usecase

import com.google.common.truth.Truth.*
import com.simprints.core.domain.image.SecuredImageRef
import com.simprints.core.domain.modality.Modality
import com.simprints.infra.images.local.ImageLocalDataSource
import com.simprints.infra.images.metadata.ImageMetadataStore
import com.simprints.infra.images.usecase.CalculateFileMd5AndSizeUseCase
import com.simprints.infra.images.usecase.SamplePathConverter
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.FileInputStream

internal class PrepareImageUploadDataUseCaseTest {
    @MockK
    lateinit var localDataSource: ImageLocalDataSource

    @MockK
    lateinit var calculateFileMd5AndSize: CalculateFileMd5AndSizeUseCase

    @MockK
    lateinit var samplePathUtil: SamplePathConverter

    @MockK
    lateinit var metadataStore: ImageMetadataStore

    @MockK
    lateinit var fileStream: FileInputStream

    @MockK
    lateinit var imageRef: SecuredImageRef

    private lateinit var useCase: PrepareImageUploadDataUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = PrepareImageUploadDataUseCase(
            localDataSource = localDataSource,
            calculateFileMd5AndSize = calculateFileMd5AndSize,
            samplePathUtil = samplePathUtil,
            metadataStore = metadataStore,
        )
    }

    @Test
    fun `Successfully prepares data for sample upload`() = runTest {
        coEvery { localDataSource.decryptImage(any()) } returns fileStream
        coEvery { calculateFileMd5AndSize(any()) } returns
            CalculateFileMd5AndSizeUseCase.CalculationResult("base64-md5", 10L)
        every { samplePathUtil.extract(any()) } returns
            SamplePathConverter.PathData("sessionId", Modality.FINGERPRINT, "sampleId")
        coEvery { metadataStore.getMetadata(any()) } returns mapOf("k" to "v")

        val result = useCase(imageRef)

        verify(exactly = 1) { fileStream.close() }
        assertThat(result?.imageRef).isEqualTo(imageRef)
        assertThat(result?.sampleId).isEqualTo("sampleId")
        assertThat(result?.sessionId).isEqualTo("sessionId")
        assertThat(result?.md5).isEqualTo("base64-md5")
        assertThat(result?.size).isEqualTo(10L)
        assertThat(result?.modality).isEqualTo("FINGERPRINT")
        assertThat(result?.metadata).isEqualTo(mapOf("k" to "v"))
    }

    @Test
    fun `Gracefully handles missing file`() = runTest {
        coEvery { localDataSource.decryptImage(any()) } returns null

        val result = useCase(imageRef)

        assertThat(result).isNull()
    }

    @Test
    fun `Gracefully handles path extraction failure`() = runTest {
        coEvery { localDataSource.decryptImage(any()) } returns fileStream
        coEvery { calculateFileMd5AndSize(any()) } returns
            CalculateFileMd5AndSizeUseCase.CalculationResult("base64-md5", 10L)
        every { samplePathUtil.extract(any()) } returns null

        val result = useCase(imageRef)

        assertThat(result).isNull()
    }
}
