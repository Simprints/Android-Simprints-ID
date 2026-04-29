package com.simprints.feature.externalcredential.screens.scanocr.reader

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.externalcredential.model.BoundingBox
import io.mockk.MockKAnnotations
import org.junit.Before
import org.junit.Test

internal class OcrReaderTest {
    private lateinit var reader: OcrReader

    private lateinit var labelMembership: OcrLine
    private lateinit var membershipValue: OcrLine
    private lateinit var labelIssueDate: OcrLine
    private lateinit var issueDateValue: OcrLine
    private lateinit var labelExpiryDate: OcrLine
    private lateinit var expiryDateValue: OcrLine

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        labelMembership = createLine(id = 0, text = "membership number", top = 100)
        membershipValue = createLine(id = 1, text = "12345678", top = 140)
        labelIssueDate = createLine(id = 2, text = "issue date", top = 200)
        issueDateValue = createLine(id = 3, text = "03/03", top = 240)
        labelExpiryDate = createLine(id = 4, text = "expiry date", top = 200, right = 200)
        expiryDateValue = createLine(id = 5, text = "11/11", top = 240, right = 200)

        reader = OcrReader(
            OcrText(
                blocks = emptyList(),
                allLines = listOf(labelMembership, membershipValue, labelExpiryDate, expiryDateValue, labelIssueDate, issueDateValue),
            ),
        )
    }

    @Test
    fun `find returns null when no lines match`() {
        val result = reader.find { containsText("$labelMembership extra") }
        assertThat(result).isNull()
    }

    @Test
    fun `find returns first line in iteration order when multiple lines match`() {
        val result = reader.find { matchesPattern(Regex("\\d+")) }
        assertThat(result).isEqualTo(membershipValue)
    }

    @Test
    fun `containsText returns first matching line`() {
        val result = reader.find { containsText("member") }
        assertThat(result).isEqualTo(labelMembership)
    }

    @Test
    fun `containsText is case-insensitive`() {
        val result = reader.find { containsText("MEMBER") }
        assertThat(result).isEqualTo(labelMembership)
    }

    @Test
    fun `hasExactText matches full string only`() {
        val result = reader.find { hasExactText(labelExpiryDate.text) }
        assertThat(result).isEqualTo(labelExpiryDate)
    }

    @Test
    fun `hasExactText returns null for partial match`() {
        val result = reader.find { hasExactText("expiry") }
        assertThat(result).isNull()
    }

    @Test
    fun `hasExactText is case-insensitive`() {
        val result = reader.find { hasExactText(labelExpiryDate.text.uppercase()) }
        assertThat(result).isEqualTo(labelExpiryDate)
    }

    @Test
    fun `matchesPattern finds 8-digit number`() {
        val result = reader.find { matchesPattern(Regex("^\\d{8}$")) }
        assertThat(result).isEqualTo(membershipValue)
    }

    @Test
    fun `matchesPattern finds date format`() {
        val result = reader.find { matchesPattern(Regex("^\\d{2}/\\d{2}$")) }
        assertThat(result).isEqualTo(expiryDateValue)
    }

    @Test
    fun `hasId finds line by id`() {
        val result = reader.find { hasId(labelExpiryDate.id) }
        assertThat(result).isEqualTo(labelExpiryDate)
    }

    @Test
    fun `hasId returns null for unknown id`() {
        val result = reader.find { hasId(labelExpiryDate.id + 99) }
        assertThat(result).isNull()
    }

    @Test
    fun `isBelow direct OcrLine returns first line below anchor`() {
        val result = reader.find { isBelow(labelMembership) }
        assertThat(result).isEqualTo(membershipValue)
    }

    @Test
    fun `isBelow block resolves anchor via text containment`() {
        val result = reader.find {
            matchesPattern(Regex("^\\d{8}$"))
            isBelow { containsText("membership") }
        }
        assertThat(result).isEqualTo(membershipValue)
    }

    @Test
    fun `isBelow block resolves anchor via pattern`() {
        val result = reader.find {
            matchesPattern(Regex("^\\d{8}$"))
            isBelow { matchesPattern(Regex("membership")) }
        }
        assertThat(result).isEqualTo(membershipValue)
    }

    @Test
    fun `isBelow block resolves anchor via line id`() {
        val result = reader.find {
            matchesPattern(Regex("^\\d{8}$"))
            isBelow { hasId(labelMembership.id) }
        }
        assertThat(result).isEqualTo(membershipValue)
    }

    @Test
    fun `isBelow returns null when anchor cannot be resolved`() {
        val result = reader.find { isBelow { containsText("$labelMembership extra") } }
        assertThat(result).isNull()
    }

    @Test
    fun `isAbove direct OcrLine returns first line above anchor`() {
        val result = reader.find { isAbove(labelExpiryDate) }
        assertThat(result).isEqualTo(labelMembership)
    }

    @Test
    fun `isAbove block resolves anchor via text containment`() {
        val result = reader.find {
            matchesPattern(Regex("^\\d{8}$"))
            isAbove { containsText("expiry date") }
        }
        assertThat(result).isEqualTo(membershipValue)
    }

    @Test
    fun `isAbove block resolves anchor via pattern`() {
        val result = reader.find {
            matchesPattern(Regex("^\\d{8}$"))
            isAbove { matchesPattern(Regex("expiry")) }
        }
        assertThat(result).isEqualTo(membershipValue)
    }

    @Test
    fun `isAbove block resolves anchor via line id`() {
        val result = reader.find {
            matchesPattern(Regex("^\\d{8}$"))
            isAbove { hasId(labelIssueDate.id) }
        }
        assertThat(result).isEqualTo(membershipValue)
    }

    @Test
    fun `isAbove returns null when anchor cannot be resolved`() {
        val result = reader.find { isAbove { containsText("nonexistent") } }
        assertThat(result).isNull()
    }

    @Test
    fun `isBelow and isAbove combined finds value sandwiched between labels`() {
        val result = reader.find {
            matchesPattern(Regex("^\\d{8}$"))
            isBelow { containsText(labelMembership.text) }
            isAbove { containsText(labelIssueDate.text) }
        }
        assertThat(result).isEqualTo(membershipValue)
    }

    @Test
    fun `isBelow and isAbove returns null when no line fits between anchors`() {
        val result = reader.find {
            isBelow(labelExpiryDate)
            isAbove(membershipValue)
        }
        assertThat(result).isNull()
    }

    private fun createLine(
        id: Int,
        text: String,
        top: Int,
        left: Int = 0,
        right: Int = 100,
    ) = OcrLine(
        id = id,
        text = text,
        boundingBox = BoundingBox(left = left, top = top, right = right, bottom = top + 30),
        blockBoundingBox = BoundingBox(left = left, top = top, right = right, bottom = top + 30),
        confidence = 1f,
    )
}
