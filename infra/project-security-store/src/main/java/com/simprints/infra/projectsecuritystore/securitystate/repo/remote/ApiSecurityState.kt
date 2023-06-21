package com.simprints.infra.projectsecuritystore.securitystate.repo.remote

import androidx.annotation.Keep
import com.simprints.infra.projectsecuritystore.securitystate.models.SecurityState

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
        PROJECT_ENDING,
        PROJECT_ENDED,
        ;

        fun fromApiToDomain(): SecurityState.Status = when (this) {
            RUNNING -> SecurityState.Status.RUNNING
            COMPROMISED -> SecurityState.Status.COMPROMISED
            PROJECT_ENDED -> SecurityState.Status.PROJECT_ENDED
            PROJECT_ENDING -> SecurityState.Status.PROJECT_ENDING
        }
    }

    fun fromApiToDomain() = SecurityState(
        deviceId,
        status.fromApiToDomain(),
        mustUpSyncEnrolmentRecords?.fromApiToDomain(),
    )
}
