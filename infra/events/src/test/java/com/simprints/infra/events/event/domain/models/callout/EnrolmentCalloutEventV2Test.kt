package com.simprints.infra.events.event.domain.models.callout

import androidx.annotation.Keep
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_ENROLMENT
import com.simprints.infra.events.event.domain.models.callout.EnrolmentCalloutEventV2.Companion.EVENT_VERSION
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_METADATA
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_USER_ID
import org.junit.Test

@Keep
class EnrolmentCalloutEventV2Test {
    @Test
    fun create_EnrolmentCalloutEvent() {
        val event = EnrolmentCalloutEventV2(
            createdAt = CREATED_AT,
            projectId = DEFAULT_PROJECT_ID,
            userId = DEFAULT_USER_ID,
            moduleId = DEFAULT_MODULE_ID,
            metadata = DEFAULT_METADATA,
        )

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(CALLOUT_ENROLMENT)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CALLOUT_ENROLMENT)
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(userId).isEqualTo(DEFAULT_USER_ID)
            assertThat(moduleId).isEqualTo(DEFAULT_MODULE_ID)
            assertThat(metadata).isEqualTo(DEFAULT_METADATA)
        }
    }

    @Test
    fun getTokenizableFields_returnsMapWithAttendantAndModuleId() {
        val event = EnrolmentCalloutEventV2(
            createdAt = CREATED_AT,
            projectId = DEFAULT_PROJECT_ID,
            userId = DEFAULT_USER_ID,
            moduleId = DEFAULT_MODULE_ID,
            metadata = DEFAULT_METADATA,
        )
        val tokenizableFields = event.getTokenizableFields()

        assertThat(tokenizableFields).hasSize(2)
        assertThat(tokenizableFields[TokenKeyType.AttendantId]).isEqualTo(DEFAULT_USER_ID)
        assertThat(tokenizableFields[TokenKeyType.ModuleId]).isEqualTo(DEFAULT_MODULE_ID)
    }
}
