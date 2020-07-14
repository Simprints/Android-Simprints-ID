package com.simprints.face.license.data.repository

interface LicenseRepository {
    suspend fun getLicense(projectId: String, deviceId: String): String?
}
