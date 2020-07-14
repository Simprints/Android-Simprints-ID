package com.simprints.face.license.data.remote

interface LicenseRemoteDataSource {
    suspend fun getLicense(projectId: String, deviceId: String): String?
}
