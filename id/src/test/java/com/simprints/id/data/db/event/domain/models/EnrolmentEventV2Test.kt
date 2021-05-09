package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.DefaultTestConstants.GUID2
import com.simprints.id.commontesttools.events.CREATED_AT
import com.simprints.id.commontesttools.events.DEFAULT_ENDED_AT
import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT_V2
import com.simprints.id.sampledata.EnrolmentEventV2Sample
import org.junit.Test

class EnrolmentEventV2Test {

    @Test
    fun create_EnrolmentEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val event = EnrolmentEventV2Sample.getEvent()
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(EventType.ENROLMENT_V2)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EnrolmentEventV2.EVENT_VERSION)
            assertThat(type).isEqualTo(ENROLMENT_V2)
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(moduleId).isEqualTo(DEFAULT_MODULE_ID)
            assertThat(attendantId).isEqualTo(DEFAULT_USER_ID)
            assertThat(personCreationEventId).isEqualTo(GUID2)
        }
    }
}

