package com.simprints.feature.externalcredential.screens.scanocr.reader

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.externalcredential.model.BoundingBox
import io.mockk.MockKAnnotations
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

internal class OcrQueryTest {
    private val noOpSubQuery: (OcrQuery) -> OcrLine? = { null }

    private lateinit var query: OcrQuery

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        query = OcrQuery(noOpSubQuery)
    }

    @Test
    fun `matchesPattern registers a filter`() {
        query.matchesPattern(Regex("\\d+"))
        assertThat(query.filters).hasSize(1)
    }

    @Test
    fun `containsPattern registers a filter`() {
        query.containsPattern(Regex("\\d+"))
        assertThat(query.filters).hasSize(1)
    }

    @Test
    fun `containsText registers a filter`() {
        query.containsText("membership")
        assertThat(query.filters).hasSize(1)
    }

    @Test
    fun `hasExactText registers a filter`() {
        query.hasExactText("membership number")
        assertThat(query.filters).hasSize(1)
    }

    @Test
    fun `hasId registers a filter`() {
        query.hasId(1)
        assertThat(query.filters).hasSize(1)
    }

    @Test
    fun `multiple filters are all registered`() {
        query.matchesPattern(Regex("\\d+"))
        query.containsText("membership")
        query.hasId(1)
        assertThat(query.filters).hasSize(3)
    }

    @Test
    fun `matchesPattern passes line whose full text matches pattern`() {
        query.matchesPattern(Regex("^\\d{8}$"))
        assertThat(query.filters.all { it(line(text = "12345678")) }).isTrue()
    }

    @Test
    fun `matchesPattern rejects line where pattern matches only a substring`() {
        query.matchesPattern(Regex("\\d{8}"))
        assertThat(query.filters.all { it(line(text = "ID:12345678")) }).isFalse()
    }

    @Test
    fun `matchesPattern rejects non-matching line`() {
        query.matchesPattern(Regex("^\\d{8}$"))
        assertThat(query.filters.all { it(line(text = "abcdefgh")) }).isFalse()
    }

    @Test
    fun `containsPattern passes line containing a partial match`() {
        query.containsPattern(Regex("membership"))
        assertThat(query.filters.all { it(line(text = "membership number")) }).isTrue()
    }

    @Test
    fun `containsPattern passes line where pattern matches full text`() {
        query.containsPattern(Regex("membership"))
        assertThat(query.filters.all { it(line(text = "membership")) }).isTrue()
    }

    @Test
    fun `containsPattern rejects line with no match`() {
        query.containsPattern(Regex("expiry"))
        assertThat(query.filters.all { it(line(text = "membership number")) }).isFalse()
    }

    @Test
    fun `containsText passes line containing text`() {
        query.containsText("member")
        assertThat(query.filters.all { it(line(text = "membership number")) }).isTrue()
    }

    @Test
    fun `containsText is case-insensitive`() {
        query.containsText("MEMBER")
        assertThat(query.filters.all { it(line(text = "membership number")) }).isTrue()
    }

    @Test
    fun `containsText rejects line not containing text`() {
        query.containsText("expiry")
        assertThat(query.filters.all { it(line(text = "membership number")) }).isFalse()
    }

    @Test
    fun `hasExactText passes line with exact text`() {
        query.hasExactText("expiry date")
        assertThat(query.filters.all { it(line(text = "expiry date")) }).isTrue()
    }

    @Test
    fun `hasExactText is case-insensitive`() {
        query.hasExactText("EXPIRY DATE")
        assertThat(query.filters.all { it(line(text = "expiry date")) }).isTrue()
    }

    @Test
    fun `hasExactText rejects partial match`() {
        query.hasExactText("expiry date")
        assertThat(query.filters.all { it(line(text = "expiry")) }).isFalse()
    }

    @Test
    fun `hasId passes line with matching id`() {
        query.hasId(2)
        assertThat(query.filters.all { it(line(id = 2)) }).isTrue()
    }

    @Test
    fun `hasId rejects line with different id`() {
        query.hasId(2)
        assertThat(query.filters.all { it(line(id = 99)) }).isFalse()
    }

    @Test
    fun `isBelow with block registers belowResolver`() {
        query.isBelow { containsText("membership") }
        assertThat(query.belowResolver).isNotNull()
    }

    @Test
    fun `isBelow with OcrLine registers belowResolver`() {
        query.isBelow(mockk<OcrLine>(relaxed = true))
        assertThat(query.belowResolver).isNotNull()
    }

    @Test
    fun `isAbove with block registers aboveResolver`() {
        query.isAbove { containsText("expiry") }
        assertThat(query.aboveResolver).isNotNull()
    }

    @Test
    fun `isAbove with OcrLine registers aboveResolver`() {
        query.isAbove(mockk<OcrLine>(relaxed = true))
        assertThat(query.aboveResolver).isNotNull()
    }

    @Test
    fun `isBelow with direct OcrLine resolver returns that line`() {
        val anchor = line(id = 0, text = "anchor")
        query.isBelow(anchor)
        assertThat(query.belowResolver?.invoke()).isEqualTo(anchor)
    }

    @Test
    fun `isAbove with direct OcrLine resolver returns that line`() {
        val anchor = line(id = 0, text = "anchor")
        query.isAbove(anchor)
        assertThat(query.aboveResolver?.invoke()).isEqualTo(anchor)
    }

    private fun line(
        id: Int = 0,
        text: String = "",
    ) = OcrLine(
        id = id,
        text = text,
        boundingBox = BoundingBox(left = 0, top = 0, right = 100, bottom = 30),
        blockBoundingBox = BoundingBox(left = 0, top = 0, right = 100, bottom = 30),
        confidence = 1f,
    )
}
