package com.simprints.core.tools.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExtractCommCareCaseIdUseCaseTest {
    private val useCase = ExtractCommCareCaseIdUseCase()

    @Test
    fun `returns null when metadata is null`() {
        val result = useCase(null)

        assertThat(result).isNull()
    }

    @Test
    fun `returns null when metadata is empty`() {
        val result = useCase("")

        assertThat(result).isNull()
    }

    @Test
    fun `returns null when metadata is blank`() {
        val result = useCase(" ")

        assertThat(result).isNull()
    }

    @Test
    fun `returns null when metadata is invalid JSON`() {
        val result = useCase("invalid json")

        assertThat(result).isNull()
    }

    @Test
    fun `returns null when metadata doesn't contain caseId`() {
        val metadata = """{"otherField": "value"}"""
        val result = useCase(metadata)

        assertThat(result).isNull()
    }

    @Test
    fun `returns null when caseId is not a string`() {
        val metadata = """{"caseId": 123}"""
        val result = useCase(metadata)

        assertThat(result).isNull()
    }

    @Test
    fun `returns caseId when metadata contains valid caseId string`() {
        val expectedCaseId = "case-123-abc"
        val metadata = """{"caseId": "$expectedCaseId"}"""
        val result = useCase(metadata)

        assertThat(result).isEqualTo(expectedCaseId)
    }

    @Test
    fun `returns caseId when metadata contains valid caseId with other fields`() {
        val expectedCaseId = "case-456-def"
        val metadata = """{"caseId": "$expectedCaseId", "otherField": "value", "number": 42}"""
        val result = useCase(metadata)

        assertThat(result).isEqualTo(expectedCaseId)
    }

    @Test
    fun `returns empty string when caseId is empty string in JSON`() {
        val metadata = """{"caseId": ""}"""
        val result = useCase(metadata)

        assertThat(result).isEmpty()
    }

    @Test
    fun `handles malformed JSON gracefully`() {
        val malformedJson = """{"caseId": "test", "incomplete": }"""
        val result = useCase(malformedJson)

        assertThat(result).isNull()
    }
}
