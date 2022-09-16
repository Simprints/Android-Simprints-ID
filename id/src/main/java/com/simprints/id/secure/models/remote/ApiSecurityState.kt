package com.simprints.id.secure.models.remote

import androidx.annotation.Keep
import com.simprints.id.secure.models.SecurityState

@Keep
data class ApiSecurityState(
    val deviceId: String,
    val status: Status,
    val mustUpSyncEnrolmentRecords: ApiUpSyncEnrolmentRecords? = null
) {

    @Keep
    enum class Status {
        RUNNING,
        COMPROMISED,
        PROJECT_ENDED;
    }
}

fun ApiSecurityState.fromApiToDomain() = SecurityState(
    deviceId,
    status.fromApiToDomain(),
    mustUpSyncEnrolmentRecords?.fromApiToDomain(),
)

fun ApiSecurityState.Status.fromApiToDomain(): SecurityState.Status =
    when (this) {
        ApiSecurityState.Status.RUNNING -> SecurityState.Status.RUNNING
        ApiSecurityState.Status.COMPROMISED -> SecurityState.Status.COMPROMISED
        ApiSecurityState.Status.PROJECT_ENDED -> SecurityState.Status.PROJECT_ENDED
    }
