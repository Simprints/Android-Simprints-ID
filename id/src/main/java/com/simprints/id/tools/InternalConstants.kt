package com.simprints.id.tools

class InternalConstants {

    class RequestIntents {
        companion object {
            private const val PREFIX = 100
            const val LOGIN_ACTIVITY_REQUEST = PREFIX + 1
            const val ALERT_ACTIVITY_REQUEST = PREFIX + 2
        }
    }
}
