package com.simprints.feature.clientapi.mappers.request.extractors

import com.google.common.truth.Truth.assertThat
import com.simprints.libsimprints.Constants.SIMPRINTS_METADATA
import com.simprints.libsimprints.Constants.SIMPRINTS_SUBJECT_AGE
import kotlin.test.Test

internal class ActionRequestExtractorTest {
    // Concrete subclass for testing
    class TestActionRequestExtractor(
        extras: Map<String, Any>,
        override val expectedKeys: List<String> = emptyList(),
    ) : ActionRequestExtractor(extras)

    @Test
    fun `returns age when valid integer present`() {
        val extractor = TestActionRequestExtractor(
            mapOf(SIMPRINTS_METADATA to """{"$SIMPRINTS_SUBJECT_AGE": 30}"""),
        )
        assertThat(extractor.getSubjectAge()).isEqualTo(30)
    }

    @Test
    fun `returns null when age key is missing`() {
        val extractor = TestActionRequestExtractor(
            mapOf(SIMPRINTS_METADATA to """{"name": "Alice"}"""),
        )
        assertThat(extractor.getSubjectAge()).isNull()
    }

    @Test
    fun `returns null when age key is not an integer`() {
        val extractor = TestActionRequestExtractor(
            mapOf(SIMPRINTS_METADATA to """{"$SIMPRINTS_SUBJECT_AGE": "twenty"}"""),
        )
        assertThat(extractor.getSubjectAge()).isNull()
    }

    @Test
    fun `returns null for invalid JSON`() {
        val extractor = TestActionRequestExtractor(
            mapOf(SIMPRINTS_METADATA to """{invalid json"""),
        )
        assertThat(extractor.getSubjectAge()).isNull()
    }

    @Test
    fun `returns null when age is null in JSON`() {
        val extractor = TestActionRequestExtractor(
            mapOf(SIMPRINTS_METADATA to """{"$SIMPRINTS_SUBJECT_AGE": null}"""),
        )
        assertThat(extractor.getSubjectAge()).isNull()
    }

    @Test
    fun `returns null when metadata key is missing`() {
        val extractor = TestActionRequestExtractor(
            emptyMap(),
        )
        assertThat(extractor.getSubjectAge()).isNull()
    }
}
