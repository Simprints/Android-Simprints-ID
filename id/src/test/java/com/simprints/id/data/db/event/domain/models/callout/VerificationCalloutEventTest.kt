package com.simprints.id.data.db.event.domain.models.callout

import androidx.annotation.Keep
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_METADATA
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.DefaultTestConstants.GUID2
import com.simprints.id.data.db.event.domain.models.CREATED_AT
import com.simprints.id.data.db.event.domain.models.DEFAULT_ENDED_AT
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.CALLOUT_VERIFICATION
import com.simprints.id.data.db.event.domain.models.callout.VerificationCalloutEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.callout.VerificationCalloutEvent.VerificationCalloutPayload
import org.junit.Test

@Keep
class VerificationCalloutEventTest {
    @Test
    fun create_VerificationCalloutEvent() {

        val event = VerificationCalloutEvent(CREATED_AT, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, GUID1, DEFAULT_METADATA, GUID2)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).containsExactly(SessionIdLabel(GUID2))
        assertThat(event.type).isEqualTo(CALLOUT_VERIFICATION)
        with(event.payload as VerificationCalloutPayload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CALLOUT_VERIFICATION)
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(userId).isEqualTo(DEFAULT_USER_ID)
            assertThat(moduleId).isEqualTo(DEFAULT_MODULE_ID)
            assertThat(verifyGuid).isEqualTo(GUID1)
            assertThat(metadata).isEqualTo(DEFAULT_METADATA)
        }
    }
}


