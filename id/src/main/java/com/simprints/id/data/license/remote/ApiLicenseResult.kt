package com.simprints.id.data.license.remote

sealed class ApiLicenseResult {
    data class Success(val licenseJson: String) : ApiLicenseResult()
    data class Error(val errorCode: String) : ApiLicenseResult()
}
