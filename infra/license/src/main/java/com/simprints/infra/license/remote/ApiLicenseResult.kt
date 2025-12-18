package com.simprints.infra.license.remote

import kotlinx.serialization.Serializable

@Serializable
internal sealed class ApiLicenseResult {
    @Serializable
    data class Success(
        val license: LicenseValue,
    ) : ApiLicenseResult()

    @Serializable
    data class Error(
        val errorCode: String,
    ) : ApiLicenseResult()

    @Serializable
    data class BackendMaintenanceError(
        val estimatedOutage: Long? = null,
    ) : ApiLicenseResult()
}
