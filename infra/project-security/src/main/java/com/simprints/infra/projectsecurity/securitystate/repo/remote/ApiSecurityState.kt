package com.simprints.infra.projectsecurity.securitystate.repo.remote

import androidx.annotation.Keep
import com.simprints.infra.projectsecurity.securitystate.models.SecurityState

@Keep
internal data class ApiSecurityState(
    val deviceId: String,
    val status: Status,
    val mustUpSyncEnrolmentRecords: ApiUpSyncEnrolmentRecords? = null
) {

    @Keep
    enum class Status {
        RUNNING,
        COMPROMISED,
        PROJECT_ENDED,
        ;

        fun fromApiToDomain(): SecurityState.Status = when (this) {
            RUNNING -> SecurityState.Status.RUNNING
            COMPROMISED -> SecurityState.Status.COMPROMISED
            PROJECT_ENDED -> SecurityState.Status.PROJECT_ENDED
        }
    }

    fun fromApiToDomain() = SecurityState(
        deviceId,
        status.fromApiToDomain(),
        mustUpSyncEnrolmentRecords?.fromApiToDomain(),
    )
}
