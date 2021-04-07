package com.simprints.id.data.license.remote

import timber.log.Timber

class LicenseRemoteDataSourceImpl(private val licenseServer: SimprintsLicenseServer) : LicenseRemoteDataSource {
    override suspend fun getLicense(projectId: String, deviceId: String): String? = try {
        licenseServer.getLicense(projectId, deviceId)
    } catch (t: Throwable) {
        Timber.e(t)
        null
    }
}
