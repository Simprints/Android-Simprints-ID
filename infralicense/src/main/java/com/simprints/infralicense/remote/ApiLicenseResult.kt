package com.simprints.infralicense.remote

internal sealed class ApiLicenseResult {
    data class Success(val licenseJson: String) : ApiLicenseResult()
    data class Error(val errorCode: String) : ApiLicenseResult()
    //TODO: add a special "Bad network connection" error?
    data class BackendMaintenanceError(val estimatedOutage: Long? = null) : ApiLicenseResult()
}
