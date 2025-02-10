package com.simprints.infra.license.remote

import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.license.models.LicenseVersion
import com.simprints.infra.license.models.Vendor
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.LICENSE
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.NetworkConnectionException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import okhttp3.ResponseBody
import retrofit2.HttpException
import javax.inject.Inject

internal class LicenseRemoteDataSourceImpl @Inject constructor(
    private val authStore: AuthStore,
    private val jsonHelper: JsonHelper,
) : LicenseRemoteDataSource {
    override suspend fun getLicense(
        projectId: String,
        deviceId: String,
        vendor: Vendor,
        version: LicenseVersion,
    ): ApiLicenseResult = try {
        getProjectApiClient().executeCall {
            it
                .getLicense(projectId, deviceId, vendor.value, version.value)
                .parseApiLicense()
                .getLicenseBasedOnVendor(vendor)
                ?.let { apiLicense -> ApiLicenseResult.Success(apiLicense) }
        } ?: ApiLicenseResult.Error(UNKNOWN_ERROR_CODE)
    } catch (t: Throwable) {
        when (t) {
            is NetworkConnectionException -> {
                Simber.i("Licence download failed due to network error", t, tag = LICENSE)
                ApiLicenseResult.Error(UNKNOWN_ERROR_CODE)
            }

            is BackendMaintenanceException -> {
                Simber.i("Licence download failed due to backend maintenance", t, tag = LICENSE)
                ApiLicenseResult.BackendMaintenanceError(t.estimatedOutage)
            }

            is SyncCloudIntegrationException -> {
                Simber.e("Licence download failed due to cloud integration error", t, tag = LICENSE)
                handleCloudException(t)
            }

            else -> {
                Simber.e("Licence download failed due to unknown error", t, tag = LICENSE)
                ApiLicenseResult.Error(UNKNOWN_ERROR_CODE)
            }
        }
    }

    /**
     * If it's a Cloud exception we need to check if it's something we can recover from or not.
     * If it's an Authorization error we can check which error code BFSID returned.
     * Anything else we can't really recover.
     */
    private fun handleCloudException(exception: SyncCloudIntegrationException): ApiLicenseResult =
        if (exception.httpStatusCode() == AUTHORIZATION_ERROR) {
            handleRetrofitException(
                exception.cause as HttpException,
            )
        } else {
            ApiLicenseResult.Error(UNKNOWN_ERROR_CODE)
        }

    private fun handleRetrofitException(exception: HttpException): ApiLicenseResult {
        val errorCode =
            exception.response()?.errorBody()?.let { getLicenseErrorCode(it) } ?: UNKNOWN_ERROR_CODE
        return ApiLicenseResult.Error(errorCode)
    }

    private fun getLicenseErrorCode(errorBody: ResponseBody): String = jsonHelper.fromJson<ApiLicenseError>(errorBody.string()).error

    private suspend fun getProjectApiClient(): SimNetwork.SimApiClient<LicenseRemoteInterface> =
        authStore.buildClient(LicenseRemoteInterface::class)

    companion object {
        private const val AUTHORIZATION_ERROR = 403
        private const val UNKNOWN_ERROR_CODE = "000"
    }
}
