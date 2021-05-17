package com.simprints.id.sampledata

import com.simprints.id.data.db.event.domain.models.AuthorizationEvent
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.sampledata.SampleDefaults.CREATED_AT
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_USER_ID

object AuthorizationEventSample : SampleEvent() {
    override fun getEvent(
        labels: EventLabels,
        isClosed: Boolean
    ): AuthorizationEvent {
        val userInfo =
            AuthorizationEvent.AuthorizationPayload.UserInfo(labels.projectId!!, DEFAULT_USER_ID)
        return AuthorizationEvent(
            CREATED_AT,
            AuthorizationEvent.AuthorizationPayload.AuthorizationResult.AUTHORIZED,
            userInfo,
            labels
        )
    }
}
