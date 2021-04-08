package com.simprints.id.data.license.remote

import com.simprints.id.data.license.repository.LicenseVendor
import com.simprints.id.exceptions.unexpected.SyncCloudIntegrationException
import com.simprints.id.network.SimApiClient
import com.simprints.id.network.SimApiClientFactory
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber

class LicenseRemoteDataSourceImpl(private val simApiClientFactory: SimApiClientFactory) : LicenseRemoteDataSource {

    override suspend fun getLicense(
        projectId: String,
        deviceId: String,
        licenseVendor: LicenseVendor
    ): ApiLicenseResult = try {
        executeCall("DownloadLicense") {
            val apiLicense = it.getLicense(projectId, deviceId, licenseVendor.name)
            ApiLicenseResult.Success(licenseJson = apiLicense.rankOneLicense?.data ?: "")
        }
    } catch (t: Throwable) {
        Timber.e(t)

        if (t is SyncCloudIntegrationException)
            handleCloudException(t)
        else
            ApiLicenseResult.Error("000")
    }

    private fun handleCloudException(exception: SyncCloudIntegrationException): ApiLicenseResult {
        return if (exception.cause is HttpException && exception.cause.code() == 403)
            handleRetrofitException(exception.cause)
        else
            ApiLicenseResult.Error("000")
    }

    private fun handleRetrofitException(exception: HttpException): ApiLicenseResult {
        val errorCode = exception.response()?.errorBody()?.let { getLicenseErrorCode(it) } ?: "000"
        return ApiLicenseResult.Error(errorCode)
    }

    /**
     * BFSID returns an error in the following format:
     * ```
     * { "error": "001" }
     * ```
     */
    private fun getLicenseErrorCode(errorBody: ResponseBody): String {
        return JSONObject(errorBody.string()).getString("error")
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
