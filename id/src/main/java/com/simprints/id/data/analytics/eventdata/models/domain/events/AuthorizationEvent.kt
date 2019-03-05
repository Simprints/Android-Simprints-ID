package com.simprints.id.data.analytics.eventdata.models.domain.events

class AuthorizationEvent(val relativeStartTime: Long,
                         val result: Result,
                         val userInfo: UserInfo?) : Event(EventType.AUTHORIZATION) {

    enum class Result {
        AUTHORIZED, NOT_AUTHORIZED
    }

    class UserInfo(val projectId: String, val userId: String)
}
