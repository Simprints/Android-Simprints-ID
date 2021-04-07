package com.simprints.id.data.license.remote

import com.simprints.id.exceptions.unexpected.SyncCloudIntegrationException
import com.simprints.id.network.SimApiClient
import com.simprints.id.network.SimApiClientFactory
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber

class LicenseRemoteDataSourceImpl(private val simApiClientFactory: SimApiClientFactory) : LicenseRemoteDataSource {

    //TODO: put a vendor enum here
    //TODO: return a resource (license or error)
    override suspend fun getLicense(projectId: String, deviceId: String): String = try {
        executeCall("DownloadLicense") {
            it.getLicense(projectId, deviceId, "RANK_ONE_FACE")
        }
    } catch (t: Throwable) {
        Timber.e(t)

        if (t is SyncCloudIntegrationException)
            handleCloudException(t)
        else
            ""
    }

    private fun handleCloudException(exception: SyncCloudIntegrationException): String {
        return if (exception.cause is HttpException && exception.cause.code() == 403)
            handleRetrofitException(exception.cause)
        else
            ""
    }

    private fun handleRetrofitException(exception: HttpException): String {
        return exception.response()?.errorBody()?.let { getLicenseErrorCode(it) } ?: "000"
    }

    /**
     * BFSID returns an error in the following format:
     * ```
     * {
     *   "error": "001"
     * }
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
