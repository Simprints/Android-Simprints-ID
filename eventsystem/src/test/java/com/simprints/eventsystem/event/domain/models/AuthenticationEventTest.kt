package com.simprints.eventsystem.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.*
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.UserInfo
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.Companion.EVENT_VERSION
import com.simprints.eventsystem.event.domain.models.EventType.AUTHENTICATION
import com.simprints.eventsystem.sampledata.SampleDefaults.CREATED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.ENDED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import org.junit.Test

class AuthenticationEventTest {

    @Test
    fun create_AuthenticationEvent() {
        createAuthenticationEvent(AUTHENTICATED)
    }

    @Test
    fun create_AuthenticationEvent_withBackendError() {
        createAuthenticationEvent(BACKEND_MAINTENANCE_ERROR)
    }

    @Test
    fun create_AuthenticationEvent_withbadCredsError() {
        createAuthenticationEvent(AuthenticationPayload.Result.BAD_CREDENTIALS)
    }

    @Test
    fun create_AuthenticationEvent_withUnknownError() {
        createAuthenticationEvent(UNKNOWN)
    }

    @Test
    fun create_AuthenticationEvent_withOfflineError() {
        createAuthenticationEvent(AuthenticationPayload.Result.OFFLINE)
    }

    @Test
    fun create_AuthenticationEvent_withTechnicalError() {
        createAuthenticationEvent(AuthenticationPayload.Result.TECHNICAL_FAILURE)
    }

    @Test
    fun create_AuthenticationEvent_withIntegrityError() {
        createAuthenticationEvent(AuthenticationPayload.Result.INTEGRITY_SERVICE_ERROR)
    }

    private fun createAuthenticationEvent(result: AuthenticationPayload.Result) {
        val labels = EventLabels(sessionId = GUID1)
        val userInfo = UserInfo(DEFAULT_PROJECT_ID, DEFAULT_USER_ID)
        val event = AuthenticationEvent(CREATED_AT, ENDED_AT, userInfo, result, labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(AUTHENTICATION)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(AUTHENTICATION)
            assertThat(userInfo).isEqualTo(userInfo)
            assertThat(result).isEqualTo(result)
        }
    }

}
