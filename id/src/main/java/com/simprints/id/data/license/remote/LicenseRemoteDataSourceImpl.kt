package com.simprints.id.data.license.remote

import com.simprints.core.exceptions.SyncCloudIntegrationException
import com.simprints.core.network.NetworkConstants.Companion.AUTHORIZATION_ERROR
import com.simprints.core.network.SimApiClient
import com.simprints.core.network.SimApiClientFactory
import com.simprints.core.tools.extentions.getEstimatedOutage
import com.simprints.core.tools.extentions.isBackendMaintenanceException
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.license.repository.LicenseVendor
import com.simprints.logging.Simber
import okhttp3.ResponseBody
import retrofit2.HttpException

class LicenseRemoteDataSourceImpl(
    private val simApiClientFactory: SimApiClientFactory,
    private val jsonHelper: JsonHelper
) : LicenseRemoteDataSource {

    private val unknownErrorCode = "000"

    override suspend fun getLicense(
        projectId: String,
        deviceId: String,
        licenseVendor: LicenseVendor
    ): ApiLicenseResult = try {
        executeCall("DownloadLicense") {
            val apiLicense = it.getLicense(projectId, deviceId, licenseVendor.name)

            ApiLicenseResult.Success(licenseJson = apiLicense.getLicenseBasedOnVendor(licenseVendor))
        }
    } catch (t: Throwable) {
        Simber.e(t)
        if (t.isBackendMaintenanceException()) {
            ApiLicenseResult.BackendMaintenanceError(t.getEstimatedOutage())
        } else if (t is SyncCloudIntegrationException) {
            handleCloudException(t)
        } else {
            ApiLicenseResult.Error(unknownErrorCode)
        }
    }

    /**
     * If it's a Cloud exception we need to check if it's something we can recover from or not.
     * If it's an Authorization error we can check which error code BFSID returned.
     * Anything else we can't really recover.
     */
    private fun handleCloudException(exception: SyncCloudIntegrationException): ApiLicenseResult {
        return if (exception.cause is HttpException && (exception.cause as HttpException).code() == AUTHORIZATION_ERROR)
            handleRetrofitException(exception.cause as HttpException)
        else
            ApiLicenseResult.Error(unknownErrorCode)
    }

    private fun handleRetrofitException(exception: HttpException): ApiLicenseResult {
        val errorCode = exception.response()?.errorBody()?.let { getLicenseErrorCode(it) } ?: unknownErrorCode
        return ApiLicenseResult.Error(errorCode)
    }

    private fun getLicenseErrorCode(errorBody: ResponseBody): String {
        return jsonHelper.fromJson<ApiLicenseError>(errorBody.string()).error
    }

    private suspend fun <T> executeCall(
        nameCall: String,
        block: suspend (LicenseRemoteInterface) -> T
    ): T =
        with(getProjectApiClient()) {
            executeCall(nameCall) {
                block(it)
            }
        }

    private suspend fun getProjectApiClient(): SimApiClient<LicenseRemoteInterface> =
        simApiClientFactory.buildClient(LicenseRemoteInterface::class)
}
