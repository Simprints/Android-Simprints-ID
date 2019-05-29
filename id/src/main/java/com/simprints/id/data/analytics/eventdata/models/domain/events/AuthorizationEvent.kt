package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class AuthorizationEvent(starTime: Long,
                         val result: Result,
                         val userInfo: UserInfo?) : Event(EventType.AUTHORIZATION, starTime) {

    @Keep
    enum class Result {
        AUTHORIZED, NOT_AUTHORIZED
    }

    @Keep
    class UserInfo(val projectId: String, val userId: String)
}
