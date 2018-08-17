package com.simprints.id.data.analytics.events.models

class AuthorizationEvent(val relativeStartTime: Long,
                         val result: AuthorizationEvent.Result,
                         val userInfo: AuthorizationEvent.Info?) : Event(EventType.AUTHORIZATION) {

    enum class Result {
        AUTHORIZED, NOT_AUTHORIZED
    }

    class Info(val projectId: String, val userId: String)
}
