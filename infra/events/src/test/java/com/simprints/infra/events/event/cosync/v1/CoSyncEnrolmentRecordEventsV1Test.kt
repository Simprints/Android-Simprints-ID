package com.simprints.infra.events.event.cosync.v1

import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEvents
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.infra.events.event.domain.models.subject.FaceReference
import com.simprints.infra.events.event.domain.models.subject.FaceTemplate
import com.simprints.infra.events.event.domain.models.subject.FingerprintReference
import com.simprints.infra.events.event.domain.models.subject.FingerprintTemplate
import org.junit.Test
import kotlin.test.assertEquals

class CoSyncEnrolmentRecordEventsV1Test {

    @Test
    fun `toCoSyncV1 and toDomain roundtrip preserves data`() {
        // Given: internal domain model
        val internalEvent = createInternalEvent()
        val internalWrapper = EnrolmentRecordEvents(listOf(internalEvent))

        // When: convert to V1 and back to domain
        val v1Wrapper = internalWrapper.toCoSyncV1()
        val domainWrapper = v1Wrapper.toDomain()

        // Then: data is preserved
        assertEquals(1, domainWrapper.events.size)
        val domainEvent = domainWrapper.events.first() as EnrolmentRecordCreationEvent
        assertEquals(internalEvent.id, domainEvent.id)
        assertEquals(internalEvent.payload.subjectId, domainEvent.payload.subjectId)
        assertEquals(internalEvent.payload.projectId, domainEvent.payload.projectId)
        assertEquals(internalEvent.payload.moduleId, domainEvent.payload.moduleId)
        assertEquals(internalEvent.payload.attendantId, domainEvent.payload.attendantId)
        assertEquals(2, domainEvent.payload.biometricReferences.size)
        assertEquals(1, domainEvent.payload.externalCredentials.size)
    }

    @Test
    fun `toCoSyncV1 sets schemaVersion to 1_0`() {
        // Given: internal domain model
        val internalWrapper = EnrolmentRecordEvents(listOf(createInternalEvent()))

        // When: convert to V1
        val v1Wrapper = internalWrapper.toCoSyncV1()

        // Then: schemaVersion is set
        assertEquals(CoSyncEnrolmentRecordEventsV1.SCHEMA_VERSION, v1Wrapper.schemaVersion)
        assertEquals("1.0", v1Wrapper.schemaVersion)
    }

    private fun createInternalEvent() = EnrolmentRecordCreationEvent(
        id = "event-1",
        payload = EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload(
            subjectId = "subject-1",
            projectId = "project-1",
            moduleId = TokenizableString.Raw("module-1"),
            attendantId = TokenizableString.Tokenized("attendant-1"),
            biometricReferences = listOf(
                FaceReference(
                    id = "face-1",
                    templates = listOf(FaceTemplate("template-1")),
                    format = "RANK_ONE",
                    metadata = mapOf("quality" to "high"),
                ),
                FingerprintReference(
                    id = "fingerprint-1",
                    templates = listOf(
                        FingerprintTemplate("fp-template-1", SampleIdentifier.LEFT_THUMB),
                    ),
                    format = "NEC",
                    metadata = null,
                ),
            ),
            externalCredentials = listOf(
                ExternalCredential(
                    id = "cred-1",
                    value = TokenizableString.Tokenized("encrypted-value"),
                    subjectId = "subject-1",
                    type = ExternalCredentialType.NHISCard,
                ),
            ),
        ),
    )
}
