package com.simprints.face.license.data.repository

import com.simprints.face.license.data.local.LicenseLocalDataSource
import com.simprints.face.license.data.remote.LicenseRemoteDataSource
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
