package com.simprints.face.license.data.repository

import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.face.license.data.local.LicenseLocalDataSource
import com.simprints.face.license.data.remote.LicenseRemoteDataSource
import kotlinx.coroutines.withContext

class LicenseRepositoryImpl(
    private val licenseLocalDataSource: LicenseLocalDataSource,
    private val licenseRemoteDataSource: LicenseRemoteDataSource,
    private val dispatcherProvider: DispatcherProvider
) : LicenseRepository {
    override suspend fun getLicense(projectId: String, deviceId: String): String? =
        withContext(dispatcherProvider.io()) {
            licenseLocalDataSource.getLicense() ?: licenseRemoteDataSource.getLicense(projectId, deviceId)?.also {
                licenseLocalDataSource.saveLicense(it)
            }
        }
}
