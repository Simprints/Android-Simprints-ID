package com.simprints.id.secure.models.remote

import com.simprints.id.secure.models.SecurityState

data class ApiSecurityState(val deviceId: String, val status: String)

fun ApiSecurityState.fromApiToDomain() = SecurityState(
    deviceId,
    SecurityState.Status.valueOf(status)
)
