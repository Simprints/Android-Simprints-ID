package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.externalcredential.model.BoundingBox
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrLine
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrReader
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrText
import io.mockk.MockKAnnotations
import org.junit.Before
import org.junit.Test

internal class GhanaIdCardOcrSelectorUseCaseTest {
    private lateinit var useCase: GhanaIdCardOcrSelectorUseCase
    private val label = "Ghana Card Number"
    private val validIds = listOf(
        "GHA-123456789-0",
        "GHA-987654321-5",
        "GHA-000000000-9",
    )
    private val invalidIds = listOf(
        "GHB-123456789-0",
        "GHA123456789-0",
        "GHA-1234567890",
        "GHA-12345678-0",
        "GHA-1234567890-0",
        "GHA-12345678A-0",
        "GHA-123456789-A",
        "GHA-123456789-01",
        "",
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = GhanaIdCardOcrSelectorUseCase()
    }

    @Test
    fun `returns matching line for valid Ghana ID formats`() {
        validIds.forEachIndexed { id, ghanaId ->
            val nonMatching = line(id = id, text = label, top = 100)
            val expected = line(id = id + 1, text = ghanaId, top = 140)
            val reader = buildReader(nonMatching, expected)

            assertThat(useCase(reader)).isEqualTo(expected)
        }
    }

    @Test
    fun `returns null for invalid Ghana ID formats`() {
        invalidIds.forEachIndexed { id, ghanaId ->
            val reader = buildReader(
                line(id = id, text = label, top = 100),
                line(id = id + 1, text = ghanaId, top = 140),
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
