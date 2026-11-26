package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.externalcredential.model.BoundingBox
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrLine
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrReader
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrText
import com.simprints.feature.externalcredential.screens.scanocr.reader.ScannedBarcode
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
    private val validFansWithWhitespace = listOf(
        "1234 5678 1234 5678",
        "12345678 12345678",
        "258094592481 5610",
        "1 23456781234567 8",
        "1234  5678 1234 5678",
        "1234 5678  1234 5678",
        "1234  5678  1234  5678",
        "2580945924  815610",
    )
    private val linesContainingFan = mapOf(
        "FAN 8642370540168134" to "8642370540168134",
        "8642370540168134 FAN" to "8642370540168134",
        "ቁጥር: 2580945924815610" to "2580945924815610",
        "hc 1234 5678 9101 1129 ab" to "1234567891011129",
    )
    private val invalidFans = listOf(
        "123456781234567", // 15 digits
        "12345678123456789", // 17 digits
        "1234567812345678 9", // 17 digits, last one separated by whitespace
        "1234567812345678  9", // 17 digits, last one separated by multiple whitespace characters
        "9  1234567812345678", // 17 digits, first one separated by multiple whitespace characters
        "1234   5678 1234 5678", // more than 2 whitespace characters between digits
        "12345678      87654321", // two separate numbers with a wide gap between them
        "ABCDEFGHIJKLMNOP", // 16 letters, 0 digits
        "1234567812345A78", // letter in the middle
        "1234-5678-1234-5678", // punctuation between digits
        "1234.5678.1234.5678", // punctuation between digits
        "07/02/1998 | 2005/10/17", // date of birth line (Gregorian | Ethiopian)
        "25/12/2000 | 2008/08/31", // date of birth line (Gregorian | Ethiopian)
        "07/02/1998 2005/10/17", // date of birth line with separator lost by OCR
        "07 02 1998 2005 10 17", // date of birth line with all separators read as spaces by OCR
        "07 02 1998 | 2005 10 17", // date of birth line with slashes read as spaces by OCR
        "07  02  1998  2005  10  17", // date of birth line with separators read as multiple spaces by OCR
        "DOB 01/2026/03 | 04/2026/04 DOB", // date-like line with surrounding text
        "1 2 3 4 5 6 7 8 1 2 3 4 5 6 7 8", // more than 4 groups
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
    fun `returns matching line and strips whitespace from FAN`() {
        validFansWithWhitespace.forEachIndexed { id, fan ->
            val reader = buildReader(line(id = id, text = fan, top = 100))
            assertThat(useCase(reader)?.credential?.text).isEqualTo(fan.filter(Char::isDigit))
        }
    }

    @Test
    fun `extracts FAN from line containing other text`() {
        linesContainingFan.entries.forEachIndexed { id, (text, fan) ->
            val reader = buildReader(line(id = id, text = text, top = 100))
            assertThat(useCase(reader)?.credential?.text).isEqualTo(fan)
        }
    }

    @Test
    fun `returns null for invalid FAN formats`() {
        invalidFans.forEachIndexed { id, fan ->
            val reader = buildReader(line(id = id, text = fan, top = 100))
            assertThat(useCase(reader)).isNull()
        }
    }

    @Test
    fun `skips date of birth line and returns FAN line`() {
        val reader = buildReader(
            line(id = 0, text = "07/02/1998 | 2005/10/17", top = 100),
            line(id = 1, text = "30/07/2026 | 08/04/2034", top = 150),
            line(id = 2, text = "8642370540168134", top = 200),
        )
        val result = useCase(reader)
        assertThat(result?.credential?.id).isEqualTo(2)
        assertThat(result?.credential?.text).isEqualTo("8642370540168134")
    }

    @Test
    fun `returns FAN from barcode`() {
        val barcodes = listOf(barcode("2580945924815610", top = 100, bottom = 140))

        val result = useCase.fromBarcodes(barcodes)

        assertThat(result?.credential?.text).isEqualTo("2580945924815610")
        assertThat(result?.credential?.confidence).isEqualTo(1f)
        assertThat(result?.isFromBarcode).isTrue()
    }

    @Test
    fun `uses barcode box as credential box`() {
        val result = useCase.fromBarcodes(listOf(barcode("2580945924815610", top = 100, bottom = 140)))

        assertThat(result?.credential?.boundingBox).isEqualTo(BoundingBox(left = 10, top = 100, right = 200, bottom = 140))
        assertThat(result?.credential?.blockBoundingBox).isEqualTo(result?.credential?.boundingBox)
    }

    @Test
    fun `skips barcodes that are not a FAN`() {
        val barcodes = listOf(
            barcode("ABC123", top = 0, bottom = 10),
            barcode("123456781234567", top = 0, bottom = 10), // 15 digits
            barcode("1234 5678 1234 5678", top = 0, bottom = 10), // separators are not expected in barcodes
            barcode("2580945924815610", top = 100, bottom = 140),
        )

        assertThat(useCase.fromBarcodes(barcodes)?.credential?.text).isEqualTo("2580945924815610")
    }

    @Test
    fun `returns null when no barcode is a FAN`() {
        assertThat(useCase.fromBarcodes(emptyList())).isNull()
        assertThat(useCase.fromBarcodes(listOf(barcode("12345678123456789", top = 0, bottom = 10)))).isNull()
    }

    private fun barcode(
        value: String,
        top: Int,
        bottom: Int,
    ) = ScannedBarcode(
        value = value,
        boundingBox = BoundingBox(left = 10, top = top, right = 200, bottom = bottom),
    )

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
