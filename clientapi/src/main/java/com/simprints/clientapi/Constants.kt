package com.simprints.clientapi

object Constants {

    const val RETURN_FOR_FLOW_COMPLETED = true
    const val RETURN_FOR_FLOW_NOT_COMPLETED = !RETURN_FOR_FLOW_COMPLETED
    const val PROJECT_ID_LENGTH = 20

    object RequestIntents {
        private const val PREFIX = 100
        const val LOGIN_ACTIVITY_REQUEST = PREFIX + 1
        const val ALERT_ACTIVITY_REQUEST = PREFIX + 2
    }

}
