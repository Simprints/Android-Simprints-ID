package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template


import android.graphics.Bitmap
import com.gemalto.wsq.WSQDecoder
import com.google.common.truth.Truth
import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.RawUnprocessedImage
import com.ygoular.bitmapconverter.BitmapConverter
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Test

class DecodeWSQImageUseCaseTest {

    private val  convertedImageBytesMock: ByteArray= emptyArray<Byte>().toByteArray()

    @MockK(relaxed = true)
    lateinit var mockedDecodeResult: WSQDecoder.WSQDecodedImage

    @MockK(relaxed = true)
    lateinit var bitmapConverter: BitmapConverter

    @MockK(relaxed = true)
    lateinit var bitmap: Bitmap

    private lateinit var decodeWsqImageUseCase: DecodeWSQImageUseCase

    companion object {
        private val UNPROCESSED_IMAGE = RawUnprocessedImage(
            byteArrayOf(
                0x05, 0x06, 0x07, 0x08, 0x05, 0x06, 0x07, 0x08,
                0x05, 0x06, 0x07, 0x08, 0x05, 0x06, 0x07, 0x08, 0x05, 0x06, 0x07, 0x08,
                0x05, 0x06, 0x07, 0x08, 0x05, 0x06, 0x07, 0x08
            )
        )
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        mockkStatic(WSQDecoder::class)
        decodeWsqImageUseCase = DecodeWSQImageUseCase(bitmapConverter)
        buildDecodedImage()
    }

    private fun buildDecodedImage() {
        every { bitmapConverter.convert(any(), any()) } returns convertedImageBytesMock
        every { mockedDecodeResult.bitmap } returns bitmap
        every { mockedDecodeResult.ppi } returns 0
    }

    @Test
    fun `test decode image success`() {
        // Given
        every { WSQDecoder.decode(any<ByteArray>()) } returns mockedDecodeResult

        // When
        val result = decodeWsqImageUseCase(UNPROCESSED_IMAGE)

        // Then
        Truth.assertThat(result.imageBytes).isEqualTo(convertedImageBytesMock)
    }

    @Test(expected = BioSdkException.ImageDecodingException::class)
    fun `test decode image failure`() {
        // Given
        every { WSQDecoder.decode(any<ByteArray>()) } returns null

        // When
        decodeWsqImageUseCase(UNPROCESSED_IMAGE)
        // Then the exception is thrown
    }

    @Test(expected = BioSdkException.ImageDecodingException::class)
    fun `test decode empty image should fail`() {
        // Given
        every { WSQDecoder.decode(any<ByteArray>()) } returns mockedDecodeResult

        // When
        decodeWsqImageUseCase(RawUnprocessedImage(byteArrayOf(0)))
        // Then the exception is thrown

    }


}
