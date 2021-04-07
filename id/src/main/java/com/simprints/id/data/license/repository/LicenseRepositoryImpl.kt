package com.simprints.id.data.license.repository

import com.simprints.id.data.license.local.LicenseLocalDataSource
import com.simprints.id.data.license.remote.LicenseRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LicenseRepositoryImpl(
    private val licenseLocalDataSource: LicenseLocalDataSource,
    private val licenseRemoteDataSource: LicenseRemoteDataSource
) : LicenseRepository {
    override fun getLicenseStates(projectId: String, deviceId: String): Flow<LicenseState> = flow {
        emit(LicenseState.Started)

        val license = licenseLocalDataSource.getLicense()
        if (license == null) {
            emit(LicenseState.Downloading)
            licenseRemoteDataSource.getLicense(projectId, deviceId)?.let {
                licenseLocalDataSource.saveLicense(it)
                emit(LicenseState.FinishedWithSuccess(it))
            } ?: emit(LicenseState.FinishedWithError)
        } else {
            emit(LicenseState.FinishedWithSuccess(license))
        }
    }

    override fun deleteCachedLicense() {
        licenseLocalDataSource.deleteCachedLicense()
    }
}
