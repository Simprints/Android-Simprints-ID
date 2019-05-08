package com.simprints.fingerprint.data.domain

class InternalConstants {

    class RequestIntents {
        companion object {
            private const val PREFIX = 200
            const val COLLECT_FINGERPRINTS_ACTIVITY_REQUEST_CODE = PREFIX + 1
            const val LONG_CONSENT_ACTIVITY_REQUEST_CODE = PREFIX + 2
            const val REFUSAL_ACTIVITY_REQUEST = PREFIX + 3
            const val MATCHING_ACTIVITY_REQUEST = PREFIX + 4
            const val ALERT_ACTIVITY_REQUEST = PREFIX + 5
            const val SIMPRINTS_VERIFY_GUID_NOT_FOUND_ONLINE = PREFIX + 6
        }
    }

    class ResultIntents {
        companion object {
            private const val PREFIX = 200
            const val ALERT_TRY_AGAIN_RESULT = PREFIX + 100
        }
    }
}
