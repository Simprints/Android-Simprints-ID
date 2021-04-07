package com.simprints.id.data.license.remote

interface LicenseRemoteDataSource {
    suspend fun getLicense(projectId: String, deviceId: String): String?
}
