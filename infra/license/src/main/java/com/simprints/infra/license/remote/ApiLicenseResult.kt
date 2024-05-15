package com.simprints.infra.license.remote

internal sealed class ApiLicenseResult {
    data class Success(val license: License) : ApiLicenseResult()
    data class Error(val errorCode: String) : ApiLicenseResult()
    data class BackendMaintenanceError(val estimatedOutage: Long? = null) : ApiLicenseResult()
}
