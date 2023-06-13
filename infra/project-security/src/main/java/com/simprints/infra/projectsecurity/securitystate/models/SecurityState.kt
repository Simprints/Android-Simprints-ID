package com.simprints.infra.projectsecurity.securitystate.models

import androidx.annotation.Keep

@Keep
data class SecurityState(
    val deviceId: String,
    val status: Status,
    val mustUpSyncEnrolmentRecords: UpSyncEnrolmentRecords? = null
) {

    @Keep
    enum class Status {
        RUNNING,
        COMPROMISED,
        PROJECT_ENDED;

        fun isCompromisedOrProjectEnded(): Boolean = this == COMPROMISED || this == PROJECT_ENDED
    }
}
