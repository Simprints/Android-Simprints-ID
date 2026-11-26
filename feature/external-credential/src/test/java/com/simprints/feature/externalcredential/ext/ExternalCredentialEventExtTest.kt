package com.simprints.feature.externalcredential.ext

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecord
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.ExternalCredentialCaptureValueEvent
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class ExternalCredentialEventExtTest {
    @Test
    fun `returns only the final capture when there were recaptures`() {
        val events = listOf(
            captureValueEvent("discardedScanId1"),
            captureValueEvent("discardedScanId2"),
            captureValueEvent("finalScanId"),
        )

        val result = events.getLinkedExternalCredentialIds(record(listOf("finalScanId")))

        assertThat(result).containsExactly("finalScanId")
    }

    @Test
    fun `returns empty list when no credential is linked to the record`() {
        val events = listOf(captureValueEvent("scanId"))

        val result = events.getLinkedExternalCredentialIds(record(emptyList()))

        assertThat(result).isEmpty()
    }

    @Test
    fun `ignores linked credentials without a capture value event in the session`() {
        val events = listOf<Event>(mockk<PersonCreationEvent>())

        val result = events.getLinkedExternalCredentialIds(record(listOf("scanId")))

        assertThat(result).isEmpty()
    }

    @Test
    fun `does not duplicate ids when a capture value event is stored more than once`() {
        val events = listOf(captureValueEvent("scanId"), captureValueEvent("scanId"))

        val result = events.getLinkedExternalCredentialIds(record(listOf("scanId")))

        assertThat(result).containsExactly("scanId")
    }

    private fun captureValueEvent(payloadId: String) = mockk<ExternalCredentialCaptureValueEvent> {
        every { payload.id } returns payloadId
    }

    private fun record(credentialIds: List<String>) = EnrolmentRecord(
        subjectId = "subjectId",
        projectId = "projectId",
        attendantId = "attendantId".asTokenizableRaw(),
        moduleId = "moduleId".asTokenizableRaw(),
        externalCredentials = credentialIds.map {
            ExternalCredential(
                id = it,
                value = "credential".asTokenizableEncrypted(),
                subjectId = "subjectId",
                type = ExternalCredentialType.NHISCard,
            )
        },
    )
}
