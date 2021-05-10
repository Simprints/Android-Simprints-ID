package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.id.sampledata.SampleDefaults.GUID1
import com.simprints.id.sampledata.SampleDefaults.CREATED_AT
import com.simprints.id.sampledata.SampleDefaults.ENDED_AT
import com.simprints.id.data.db.event.domain.models.AuthenticationEvent.AuthenticationPayload
import com.simprints.id.data.db.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.AUTHENTICATED
import com.simprints.id.data.db.event.domain.models.AuthenticationEvent.AuthenticationPayload.UserInfo
import com.simprints.id.data.db.event.domain.models.AuthenticationEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.EventType.AUTHENTICATION
import org.junit.Test

class AuthenticationEventTest {

    @Test
    fun create_AuthenticationEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val userInfo = UserInfo(DEFAULT_PROJECT_ID, DEFAULT_USER_ID)
        val event = AuthenticationEvent(CREATED_AT, ENDED_AT, userInfo, AUTHENTICATED, labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(AUTHENTICATION)
        with(event.payload as AuthenticationPayload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(AUTHENTICATION)
            assertThat(userInfo).isEqualTo(userInfo)
            assertThat(result).isEqualTo(AUTHENTICATED)
        }
    }
}
