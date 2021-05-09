package com.simprints.id.data.db.event.domain.models.callout

import androidx.annotation.Keep
import com.google.common.truth.Truth.assertThat
import com.simprints.id.sampledata.DefaultTestConstants.DEFAULT_METADATA
import com.simprints.id.sampledata.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.sampledata.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.sampledata.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.sampledata.DefaultTestConstants.GUID1
import com.simprints.id.sampledata.DefaultTestConstants.CREATED_AT
import com.simprints.id.sampledata.DefaultTestConstants.DEFAULT_ENDED_AT
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventType.CALLOUT_LAST_BIOMETRICS
import com.simprints.id.data.db.event.domain.models.callout.EnrolmentLastBiometricsCalloutEvent.Companion.EVENT_VERSION
import org.junit.Test

@Keep
class EnrolmentLastBiometricsCalloutEventTest {
    @Test
    fun create_EnrolmentLastBiometricsCalloutEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val event = EnrolmentLastBiometricsCalloutEvent(CREATED_AT, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_METADATA, GUID1, labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(CALLOUT_LAST_BIOMETRICS)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CALLOUT_LAST_BIOMETRICS)
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(userId).isEqualTo(DEFAULT_USER_ID)
            assertThat(moduleId).isEqualTo(DEFAULT_MODULE_ID)
            assertThat(metadata).isEqualTo(DEFAULT_METADATA)
        }
    }
}
