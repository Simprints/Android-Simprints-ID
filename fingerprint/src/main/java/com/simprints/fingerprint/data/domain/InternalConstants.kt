package com.simprints.fingerprint.data.domain

@Deprecated("Redone in new orchestrator")
class InternalConstants {
    @Deprecated("Redone in new orchestrator")
    class RequestIntents {
        companion object {
            private const val PREFIX = 200
            const val COLLECT_FINGERPRINTS_ACTIVITY_REQUEST_CODE = PREFIX + 1
            const val REFUSAL_ACTIVITY_REQUEST = PREFIX + 3
            const val MATCHING_ACTIVITY_REQUEST = PREFIX + 4
            const val ALERT_ACTIVITY_REQUEST = PREFIX + 5
        }
    }
}
