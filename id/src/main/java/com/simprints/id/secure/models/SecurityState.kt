package com.simprints.id.secure.models

import androidx.annotation.Keep

@Keep
data class SecurityState(val deviceId: String, val status: Status) {

    @Keep
    enum class Status {
        RUNNING,
        COMPROMISED,
        PROJECT_ENDED;

        fun isCompromisedOrProjectEnded(): Boolean = this == COMPROMISED || this == PROJECT_ENDED

    }
}
