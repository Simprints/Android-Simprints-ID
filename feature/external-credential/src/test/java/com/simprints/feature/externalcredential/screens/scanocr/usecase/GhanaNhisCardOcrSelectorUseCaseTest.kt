package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.externalcredential.model.BoundingBox
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrLine
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrReader
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrText
import io.mockk.MockKAnnotations
import org.junit.Before
import org.junit.Test

internal class GhanaNhisCardOcrSelectorUseCaseTest {
    private lateinit var useCase: GhanaNhisCardOcrSelectorUseCase
    private val label = "membership number"
    private val validNumbers = listOf(
        "12345678",
        "98765432",
        "00000000",
    )
    private val invalidNumbers = listOf(
        "1234567",
        "123456789",
        "1234567A",
        "12345-78",
        "",
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = GhanaNhisCardOcrSelectorUseCase()
    }

    @Test
    fun `returns matching line for valid NHIS membership numbers`() {
        validNumbers.forEachIndexed { id, number ->
            val label = line(id = id, text = label, top = 100)
            val expected = line(id = id + 1, text = number, top = 140)
            val reader = buildReader(label, expected)

            assertThat(useCase(reader)).isEqualTo(expected)
        }
    }

    @Test
    fun `returns null for invalid NHIS membership numbers`() {
        invalidNumbers.forEachIndexed { id, number ->
            val reader = buildReader(
                line(id = id, text = "membership number", top = 100),
                line(id = id + 1, text = number, top = 140),
            )

            assertThat(useCase(reader)).isNull()
        }
    }

    private fun buildReader(vararg lines: OcrLine) = OcrReader(
        OcrText(blocks = emptyList(), allLines = lines.toList()),
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
