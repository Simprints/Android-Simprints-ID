package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.EventType.PERSON_CREATION
import com.simprints.infra.events.event.domain.models.PersonCreationEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2
import org.junit.Test

class PersonCreationEventTest {
    @Test
    fun create_PersonCreationEvent() {
        val fingerprintCaptureEventIds = listOf(GUID1)
        val faceCaptureEventIds = listOf(GUID2)
        val event = PersonCreationEvent(
            startTime = CREATED_AT,
            fingerprintCaptureIds = fingerprintCaptureEventIds,
            fingerprintReferenceId = GUID1,
            faceCaptureIds = faceCaptureEventIds,
            faceReferenceId = GUID2,
        )

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(PERSON_CREATION)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(PERSON_CREATION)
            assertThat(fingerprintCaptureIds).isEqualTo(fingerprintCaptureEventIds)
            assertThat(fingerprintReferenceId).isEqualTo(GUID1)
            assertThat(faceCaptureEventIds).isEqualTo(faceCaptureEventIds)
            assertThat(faceReferenceId).isEqualTo(GUID2)
        }
    }

    @Test
    fun `hasFingerprintReference() returns true when fingerprintReferenceId is not null`() {
        val event = PersonCreationEvent(
            startTime = CREATED_AT,
            fingerprintCaptureIds = listOf(GUID1),
            fingerprintReferenceId = GUID1,
            faceCaptureIds = listOf(GUID2),
            faceReferenceId = GUID2,
        )

        assertThat(event.hasFingerprintReference()).isTrue()
    }

    @Test
    fun `hasFingerprintReference() returns false when fingerprintReferenceId is null`() {
        val event = PersonCreationEvent(
            startTime = CREATED_AT,
            fingerprintCaptureIds = null,
            fingerprintReferenceId = null,
            faceCaptureIds = listOf(GUID2),
            faceReferenceId = GUID2,
        )

        assertThat(event.hasFingerprintReference()).isFalse()
    }

    @Test
    fun `hasFaceReference() returns true when faceReferenceId is not null`() {
        val event = PersonCreationEvent(
            startTime = CREATED_AT,
            fingerprintCaptureIds = listOf(GUID1),
            fingerprintReferenceId = GUID1,
            faceCaptureIds = listOf(GUID2),
            faceReferenceId = GUID2,
        )

        assertThat(event.hasFaceReference()).isTrue()
    }

    @Test
    fun `hasFaceReference() returns false when faceReferenceId is null`() {
        val event = PersonCreationEvent(
            startTime = CREATED_AT,
            fingerprintCaptureIds = listOf(GUID1),
            fingerprintReferenceId = GUID1,
            faceCaptureIds = null,
            faceReferenceId = null,
        )

        assertThat(event.hasFaceReference()).isFalse()
    }

    @Test
    fun `hasBiometricData() returns true when fingerprintCaptureIds and faceCaptureIds are not null`() {
        val event = PersonCreationEvent(
            startTime = CREATED_AT,
            fingerprintCaptureIds = listOf(GUID1),
            fingerprintReferenceId = GUID1,
            faceCaptureIds = listOf(GUID2),
            faceReferenceId = GUID2,
        )

        assertThat(event.hasBiometricData()).isTrue()
    }

    @Test
    fun `hasBiometricData() returns true when faceCaptureIds is not null`() {
        val event = PersonCreationEvent(
            startTime = CREATED_AT,
            fingerprintCaptureIds = null,
            fingerprintReferenceId = null,
            faceCaptureIds = listOf(GUID2),
            faceReferenceId = GUID2,
        )

        assertThat(event.hasBiometricData()).isTrue()
    }

    @Test
    fun `hasBiometricData() returns true when fingerprintCaptureIds is not null`() {
        val event = PersonCreationEvent(
            startTime = CREATED_AT,
            fingerprintCaptureIds = listOf(GUID1),
            fingerprintReferenceId = GUID1,
            faceCaptureIds = null,
            faceReferenceId = null,
        )

        assertThat(event.hasBiometricData()).isTrue()
    }

    @Test
    fun `hasBiometricData() returns false when fingerprintCaptureIds and faceCaptureIds are null`() {
        val event = PersonCreationEvent(
            startTime = CREATED_AT,
            fingerprintCaptureIds = null,
            fingerprintReferenceId = null,
            faceCaptureIds = null,
            faceReferenceId = null,
        )

        assertThat(event.hasBiometricData()).isFalse()
    }
}
