package com.simprints.id.tools

class InternalConstants {

    class RequestIntents {
        companion object {
            private const val PREFIX = 100
            const val LOGIN_ACTIVITY_REQUEST = PREFIX + 1
            const val LAUNCH_ACTIVITY_REQUEST = PREFIX + 2
            const val ALERT_ACTIVITY_REQUEST = PREFIX + 3
            const val REFUSAL_ACTIVITY_REQUEST = PREFIX + 3
        }
    }

    class ResultIntents {
        companion object {
            private const val PREFIX = 100
            const val ALERT_TRY_AGAIN_RESULT = PREFIX + 100
        }
    }
}
