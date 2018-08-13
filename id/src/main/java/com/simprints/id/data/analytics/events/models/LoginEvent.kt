package com.simprints.id.data.analytics.events.models

class LoginEvent(val relativeStartTime: Long,
                 val relativeEndTime: Long,
                 val providedLoginInfo: LoginInfo,
                 val result: Result) : Event(EventType.LOGIN) {

    class LoginInfo(projecId: String, userId: String)

    enum class Result {
        SUCCESS,
        FAILURE
    }
}
