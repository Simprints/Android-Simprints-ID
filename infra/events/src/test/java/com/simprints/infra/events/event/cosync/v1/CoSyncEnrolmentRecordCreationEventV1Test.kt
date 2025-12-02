package com.simprints.infra.events.event.cosync.v1

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEvents
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CoSyncEnrolmentRecordCreationEventV1Test {

    @Test
    fun `toCoSyncV1 with empty externalCredentials sets null`() {
        // Given: internal domain model with empty credentials
        val internalEvent = EnrolmentRecordCreationEvent(
            id = "event-1",
            payload = EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload(
                subjectId = "subject-1",
                projectId = "project-1",
                moduleId = TokenizableString.Raw("module-1"),
                attendantId = TokenizableString.Tokenized("attendant-1"),
                biometricReferences = emptyList(),
                externalCredentials = emptyList(),
            ),
        )
        val internalWrapper = EnrolmentRecordEvents(listOf(internalEvent))

        // When: convert to V1
        val v1Wrapper = internalWrapper.toCoSyncV1()

        // Then: externalCredentials is null (not empty list)
        val v1Event = v1Wrapper.events.first() as CoSyncEnrolmentRecordCreationEventV1
        assertNull(v1Event.payload.externalCredentials)
    }

    @Test
    fun `toDomain with null externalCredentials creates empty list`() {
        // Given: V1 model with null credentials
        val v1Event = CoSyncEnrolmentRecordCreationEventV1(
            id = "event-1",
            payload = CoSyncEnrolmentRecordCreationPayloadV1(
                subjectId = "subject-1",
                projectId = "project-1",
                moduleId = TokenizableStringV1.Raw("module-1"),
                attendantId = TokenizableStringV1.Tokenized("attendant-1"),
                biometricReferences = emptyList(),
                externalCredentials = null,
            ),
        )
        val v1Wrapper = CoSyncEnrolmentRecordEventsV1(events = listOf(v1Event))

        // When: convert to domain
        val domainWrapper = v1Wrapper.toDomain()

        // Then: externalCredentials is empty list
        val domainEvent = domainWrapper.events.first() as EnrolmentRecordCreationEvent
        assertNotNull(domainEvent.payload.externalCredentials)
        assertEquals(0, domainEvent.payload.externalCredentials.size)
    }

    @Test
    fun `toCoSyncV1 and toDomain roundtrip preserves all payload fields`() {
        // Given: internal event with all fields populated
        val internalEvent = EnrolmentRecordCreationEvent(
            id = "event-123",
            payload = EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload(
                subjectId = "subject-456",
                projectId = "project-789",
                moduleId = TokenizableString.Tokenized("encrypted-module"),
                attendantId = TokenizableString.Raw("attendant-id"),
                biometricReferences = emptyList(),
                externalCredentials = emptyList(),
            ),
        )

        // When: convert to V1 and back
        val v1Event = internalEvent.toCoSyncV1()
        val domainEvent = v1Event.toDomain()

        // Then: all fields are preserved
        assertEquals(internalEvent.id, domainEvent.id)
        assertEquals(internalEvent.payload.subjectId, domainEvent.payload.subjectId)
        assertEquals(internalEvent.payload.projectId, domainEvent.payload.projectId)
        assertEquals(internalEvent.payload.moduleId, domainEvent.payload.moduleId)
        assertEquals(internalEvent.payload.attendantId, domainEvent.payload.attendantId)
    }
}
