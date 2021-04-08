package com.simprints.id.data.license.remote

import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.license.repository.LicenseVendor
import com.simprints.id.exceptions.unexpected.SyncCloudIntegrationException
import com.simprints.id.network.NetworkConstants.Companion.AUTHORIZATION_ERROR
import com.simprints.id.network.SimApiClient
import com.simprints.id.network.SimApiClientFactory
import okhttp3.ResponseBody
import retrofit2.HttpException
import timber.log.Timber

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
        Timber.e(t)

        if (t is SyncCloudIntegrationException)
            handleCloudException(t)
        else
            ApiLicenseResult.Error(unknownErrorCode)
    }

    /**
     * If it's a Cloud exception we need to check if it's something we can recover from or not.
     * If it's an Authorization error we can check which error code BFSID returned.
     * Anything else we can't really recover.
     */
    private fun handleCloudException(exception: SyncCloudIntegrationException): ApiLicenseResult {
        return if (exception.cause is HttpException && exception.cause.code() == AUTHORIZATION_ERROR)
            handleRetrofitException(exception.cause)
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

    private suspend fun <T> executeCall(nameCall: String, block: suspend (LicenseRemoteInterface) -> T): T =
        with(getProjectApiClient()) {
            executeCall(nameCall) {
                block(it)
            }
        }

    private suspend fun getProjectApiClient(): SimApiClient<LicenseRemoteInterface> =
        simApiClientFactory.buildClient(LicenseRemoteInterface::class)
}
