package com.simprints.infra.license.remote


internal interface LicenseRemoteDataSource {
    suspend fun getLicense(projectId: String, deviceId: String, vendor: String): ApiLicenseResult
}
