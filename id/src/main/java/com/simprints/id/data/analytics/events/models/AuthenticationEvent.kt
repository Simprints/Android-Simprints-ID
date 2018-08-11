package com.simprints.id.data.analytics.events.models

class AuthenticationEvent(val relativeStartTime: Long,
                          val result: AuthenticationEvent.Result,
                          val signedInUserInfo: AuthenticationEvent.Info): Event(EventType.AUTHENTICATION) {

    enum class Result {
        SUCCESS, FAILURE
    }

    class Info(val projectId: String, val userId: String)
}

