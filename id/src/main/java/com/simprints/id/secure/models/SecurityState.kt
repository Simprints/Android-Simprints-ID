package com.simprints.id.secure.models

data class SecurityState(val deviceId: String, val status: Status) {

    enum class Status {
        RUNNING,
        COMPROMISED,
        PROJECT_ENDED;

        fun isCompromisedOrProjectEnded(): Boolean = this == COMPROMISED || this == PROJECT_ENDED

    }

}
