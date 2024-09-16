package com.simprints.infra.license

import com.simprints.infra.license.local.LicenseLocalDataSource
import com.simprints.infra.license.remote.ApiLicenseResult
import com.simprints.infra.license.remote.License
import com.simprints.infra.license.remote.LicenseRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

internal class LicenseRepositoryImpl @Inject constructor(
    private val licenseLocalDataSource: LicenseLocalDataSource,
    private val licenseRemoteDataSource: LicenseRemoteDataSource,
) : LicenseRepository {

    override fun redownloadLicence(
        projectId: String,
        deviceId: String,
        licenseVendor: Vendor
    ): Flow<LicenseState> = flow {
        licenseLocalDataSource.deleteCachedLicense(licenseVendor)
        emitAll(getLicenseStates(projectId, deviceId, licenseVendor))
    }

    override fun getLicenseStates(
        projectId: String,
        deviceId: String,
        licenseVendor: Vendor
    ): Flow<LicenseState> = flow {
        emit(LicenseState.Started)

        val license = licenseLocalDataSource.getLicense(licenseVendor)
        if (license == null) {
            emit(LicenseState.Downloading)
            licenseRemoteDataSource.getLicense(projectId, deviceId, licenseVendor)
                .let { result ->
                    when (result) {
                        is ApiLicenseResult.Success -> handleLicenseResultSuccess(
                            licenseVendor,
                            result.license
                        )

                        is ApiLicenseResult.Error -> handleLicenseResultError(result)
                        is ApiLicenseResult.BackendMaintenanceError -> handleLicenseResultBackendMaintenanceError(
                            result
                        )
                    }
                }
        } else {
            emit(LicenseState.FinishedWithSuccess(license))
        }
    }

    /**
     * Get cached license
     * @throws IllegalStateException if no cached license found
     * @param licenseVendor
     * @return cached license as [String]
     */
    override suspend fun getCachedLicense(licenseVendor: Vendor) =
        licenseLocalDataSource.getLicense(licenseVendor)


    private suspend fun FlowCollector<LicenseState>.handleLicenseResultSuccess(
        licenseVendor: Vendor,
        apiLicenseResult: License
    ) {
        licenseLocalDataSource.saveLicense(licenseVendor, apiLicenseResult)
        emit(LicenseState.FinishedWithSuccess(apiLicenseResult))
    }

    private suspend fun FlowCollector<LicenseState>.handleLicenseResultError(apiLicenseResult: ApiLicenseResult.Error) {
        emit(LicenseState.FinishedWithError(apiLicenseResult.errorCode))
    }

    private suspend fun FlowCollector<LicenseState>.handleLicenseResultBackendMaintenanceError(
        apiLicenseResult: ApiLicenseResult.BackendMaintenanceError
    ) {
        emit(LicenseState.FinishedWithBackendMaintenanceError(apiLicenseResult.estimatedOutage))
    }

    override suspend fun deleteCachedLicense(licenseVendor: Vendor) = licenseLocalDataSource.deleteCachedLicense(licenseVendor)

    override suspend fun deleteCachedLicenses() = licenseLocalDataSource.deleteCachedLicenses()
}
