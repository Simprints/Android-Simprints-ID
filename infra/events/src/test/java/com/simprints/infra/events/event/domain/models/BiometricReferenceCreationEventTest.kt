package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.BiometricReferenceCreationEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.event.domain.models.EventType.BIOMETRIC_REFERENCE_CREATION
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2
import com.simprints.infra.events.sampledata.SampleDefaults.GUID3
import org.junit.Test

class BiometricReferenceCreationEventTest {
    @Test
    fun create_BiometricReferenceCreationEvent() {
        val event = BiometricReferenceCreationEvent(
            CREATED_AT,
            GUID1,
            BiometricReferenceCreationEvent.BiometricReferenceModality.FACE,
            listOf(GUID2, GUID3),
        )

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(BIOMETRIC_REFERENCE_CREATION)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isNull()
            assertThat(id).isEqualTo(GUID1)
            assertThat(modality).isEqualTo(BiometricReferenceCreationEvent.BiometricReferenceModality.FACE)
            assertThat(captureIds).containsExactly(GUID2, GUID3)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(BIOMETRIC_REFERENCE_CREATION)
        }
    }
}
