package com.simprints.id.sampledata

import com.simprints.id.data.db.event.domain.models.AuthorizationEvent
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.sampledata.DEFAULTS.CREATED_AT
import com.simprints.id.sampledata.DEFAULTS.DEFAULT_PROJECT_ID
import com.simprints.id.sampledata.DEFAULTS.DEFAULT_USER_ID

object AuthorizationEventSample : SampleEvent() {
    override fun getEvent(
        sessionId: String,
        subjectId: String,
        isClosed: Boolean
    ): AuthorizationEvent {
        val labels = EventLabels(sessionId = sessionId)
        val userInfo =
            AuthorizationEvent.AuthorizationPayload.UserInfo(DEFAULT_PROJECT_ID, DEFAULT_USER_ID)
        return AuthorizationEvent(
            CREATED_AT,
            AuthorizationEvent.AuthorizationPayload.AuthorizationResult.AUTHORIZED,
            userInfo,
            labels
        )
    }
}
