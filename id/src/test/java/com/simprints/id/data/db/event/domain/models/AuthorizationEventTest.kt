package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.data.db.event.domain.models.AuthorizationEvent.AuthorizationPayload
import com.simprints.id.data.db.event.domain.models.AuthorizationEvent.AuthorizationPayload.AuthorizationResult.AUTHORIZED
import com.simprints.id.data.db.event.domain.models.AuthorizationEvent.AuthorizationPayload.UserInfo
import com.simprints.id.data.db.event.domain.models.AuthorizationEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.EventType.AUTHORIZATION
import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.events.CREATED_AT
import com.simprints.id.commontesttools.events.DEFAULT_ENDED_AT
import org.junit.Test

class AuthorizationEventTest {

    @Test
    fun create_AuthorizationEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val userInfo = UserInfo(DEFAULT_PROJECT_ID, DEFAULT_USER_ID)
        val event = AuthorizationEvent(CREATED_AT, AUTHORIZED, userInfo, labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(AUTHORIZATION)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(AUTHORIZATION)
            assertThat(userInfo).isEqualTo(userInfo)
            assertThat(result).isEqualTo(AUTHORIZED)
        }
    }
}
