package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import com.google.common.truth.Truth
import com.secugen.sgimage.SgImage
import com.simprints.sgimagecorrection.SecugenImageCorrection
import com.simprints.sgimagecorrection.SecugenImageCorrectionException
import com.simprints.sgimagecorrection.SecugenWrapper
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SecugenImageCorrectionTest {


    @RelaxedMockK
    private lateinit var  secugenWrapper: SecugenWrapper

    private lateinit var secugenImageCorrection: SecugenImageCorrection

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        secugenImageCorrection = SecugenImageCorrection(secugenWrapper)
    }

    @Test
    fun `test secugen image convert success`() {
        // When
        val result =
            secugenImageCorrection.processRawImage(emptyByteArray, scannerConfig)
        // Then
        Truth.assertThat(result.isSuccess).isEqualTo(true)
        Truth.assertThat(result.getOrNull()).isNotNull()

    }

    @Test
    fun `test secugen image convert failure if secugen init failed `() {
        // Given
        val initializerError = SecugenImageCorrectionException(SgImage.ERROR_INITIALIZE_FAILED)
        every { secugenWrapper.initData(any()) } throws   initializerError
        // When
        val result =
            secugenImageCorrection.processRawImage(emptyByteArray, scannerConfig)
        // Then
        Truth.assertThat(result.isFailure).isEqualTo(true)
        result.fold(
            { Assert.fail("Secugen correction Shouldn't succeed") },
            {
                val exception = (it as SecugenImageCorrectionException)
                Truth.assertThat(exception).isEqualTo(initializerError)
            }
        )
    }

    @Test
    fun `test secugen image convert failure if secugen get image  failed `() {
        // Given
        val imageProcessingError = SecugenImageCorrectionException(SgImage.ERROR_WRONG_IMAGE)

        every { secugenWrapper.getImage(any(),any(),any(),any()) } throws imageProcessingError
        // When
        val result =
            secugenImageCorrection.processRawImage(emptyByteArray,scannerConfig)
        // Then
        Truth.assertThat(result.isFailure).isEqualTo(true)
        result.fold(
            { Assert.fail("Secugen correction Shouldn't succeed") },
            {
                val exception = (it as SecugenImageCorrectionException)
                Truth.assertThat(exception).isEqualTo(imageProcessingError)
            }
        )
    }

    private val emptyByteArray
        get() = emptyArray<Byte>().toByteArray()
    private val scannerConfig
        get() = SecugenImageCorrection.ScannerConfig(emptyByteArray,0,emptyByteArray,0)
}

