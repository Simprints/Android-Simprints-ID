package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.externalcredential.model.BoundingBox
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrLine
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrReader
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrText
import io.mockk.MockKAnnotations
import org.junit.Before
import org.junit.Test

internal class FaydaCardOcrReaderUseCaseTest {
    private lateinit var useCase: FaydaCardOcrReaderUseCase
    private val validFans = listOf(
        "1234567812345678",
        "0000000000000000",
        "9999999999999999",
    )
    private val validFansWithNoise = listOf(
        "1234 5678 1234 5678",
        "1234-5678-1234-5678",
        "1234.5678.1234.5678",
    )
    private val invalidFans = listOf(
        "123456781234567",   // 15 digits
        "12345678123456789", // 17 digits
        "ABCDEFGHIJKLMNOP", // 16 letters, 0 digits
        "1234567812345A78", // letter in the middle
        "",
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = FaydaCardOcrReaderUseCase()
    }

    @Test
    fun `returns matching line for valid FAN formats`() {
        validFans.forEachIndexed { id, fan ->
            val reader = buildReader(line(id = id, text = fan, top = 100))
            assertThat(useCase(reader)?.credential?.text).isEqualTo(fan)
        }
    }

    @Test
    fun `returns matching line and strips non-digit characters from FAN`() {
        validFansWithNoise.forEachIndexed { id, fan ->
            val reader = buildReader(line(id = id, text = fan, top = 100))
            assertThat(useCase(reader)?.credential?.text).isEqualTo(fan.filter(Char::isDigit))
        }
    }

    @Test
    fun `returns null for invalid FAN formats`() {
        invalidFans.forEachIndexed { id, fan ->
            val reader = buildReader(line(id = id, text = fan, top = 100))
            assertThat(useCase(reader)).isNull()
        }
    }

    private fun buildReader(vararg lines: OcrLine) = OcrReader(
        OcrText(allLines = lines.toList()),
    )

    private fun line(
        id: Int,
        text: String,
        top: Int,
    ) = OcrLine(
        id = id,
        text = text,
        boundingBox = BoundingBox(left = 0, top = top, right = 200, bottom = top + 30),
        blockBoundingBox = BoundingBox(left = 0, top = top, right = 200, bottom = top + 30),
        confidence = 1f,
    )
}
