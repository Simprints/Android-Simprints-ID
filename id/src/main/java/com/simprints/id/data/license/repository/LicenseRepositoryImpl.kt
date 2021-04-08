package com.simprints.id.data.license.repository

import com.simprints.id.data.license.local.LicenseLocalDataSource
import com.simprints.id.data.license.remote.ApiLicenseResult
import com.simprints.id.data.license.remote.LicenseRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
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
            licenseRemoteDataSource.getLicense(projectId, deviceId).let { apiLicenseResult ->
                when (apiLicenseResult) {
                    is ApiLicenseResult.Success -> handleLicenseResultSuccess(apiLicenseResult)
                    is ApiLicenseResult.Error -> handleLicenseResultError(apiLicenseResult)
                }
            }
        } else {
            emit(LicenseState.FinishedWithSuccess(license))
        }
    }

    private suspend fun FlowCollector<LicenseState>.handleLicenseResultSuccess(apiLicenseResult: ApiLicenseResult.Success) {
        licenseLocalDataSource.saveLicense(apiLicenseResult.licenseJson)
        emit(LicenseState.FinishedWithSuccess(apiLicenseResult.licenseJson))
    }

    private suspend fun FlowCollector<LicenseState>.handleLicenseResultError(apiLicenseResult: ApiLicenseResult.Error) {
        emit(LicenseState.FinishedWithError(apiLicenseResult.errorCode))
    }

    override fun deleteCachedLicense() {
        licenseLocalDataSource.deleteCachedLicense()
    }
}
