package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.AuthorizationEvent.AuthorizationPayload.AuthorizationResult.AUTHORIZED
import com.simprints.infra.events.event.domain.models.AuthorizationEvent.AuthorizationPayload.UserInfo
import com.simprints.infra.events.event.domain.models.AuthorizationEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.event.domain.models.EventType.AUTHORIZATION
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_USER_ID
import org.junit.Test

class AuthorizationEventTest {
    @Test
    fun create_AuthorizationEvent() {
        val userInfo = UserInfo(DEFAULT_PROJECT_ID, DEFAULT_USER_ID)
        val event = AuthorizationEvent(CREATED_AT, AUTHORIZED, userInfo)
        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(AUTHORIZATION)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(AUTHORIZATION)
            assertThat(userInfo).isEqualTo(userInfo)
            assertThat(result).isEqualTo(AUTHORIZED)
        }
    }
}
