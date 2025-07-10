package com.simprints.infra.images.usecase

import com.google.common.truth.Truth.*
import com.simprints.core.tools.utils.EncodingUtils
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.util.Base64

internal class CalculateFileMd5AndSizeUseCaseTest {
    // Core version relies on Android implementation, that does not work in tests
    private val encodingUtil = object : EncodingUtils {
        override fun byteArrayToBase64(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)

        override fun base64ToBytes(base64: String): ByteArray = TODO("No-op")
    }

    private val testByteArray: ByteArray
        get() = "testString".toByteArray(Charsets.UTF_8)

    private lateinit var useCase: CalculateFileMd5AndSizeUseCase

    @Before
    fun setUp() {
        useCase = CalculateFileMd5AndSizeUseCase(encodingUtil)
    }

    @Test
    fun `correctly computes MD5 checksum of provided input stream`() = runTest {
        val stream = ByteArrayInputStream(testByteArray)

        val result = useCase.invoke(stream)

        // Calculated using online calculator
        assertThat(result.md5).isEqualTo("U2eI9Nvf/uz7uPNQqUHuow==")
        assertThat(result.size).isEqualTo(testByteArray.size)
    }

    @Test
    fun `correctly computes MD5 checksum of provided input stream with multiple buffer reads`() = runTest {
        val largeBufferSize = 30 * 1024
        val stream = ByteArrayInputStream(ByteArray(largeBufferSize, { 1 }))

        val result = useCase.invoke(stream)

        assertThat(result.size).isEqualTo(largeBufferSize)
    }
}
