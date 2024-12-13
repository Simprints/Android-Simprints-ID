package com.simprints.infra.network.apiclient

import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.SimRemoteInterface
import com.simprints.infra.network.coroutines.retryIO
import com.simprints.infra.network.exceptions.ApiError
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.NetworkConnectionException
import com.simprints.infra.network.exceptions.RetryableCloudException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import com.simprints.infra.network.exceptions.isCausedFromBadNetworkConnection
import com.simprints.infra.network.httpclient.DefaultOkHttpClientBuilder
import com.simprints.infra.network.json.JsonHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import kotlin.reflect.KClass

/**
 * This class isn't marked internal yet because we need to build it currently in other modules'
 * android tests, which can't be accessed via DI yet. Once the testing DI is cleaned up this class
 * can be marked internal.
 */
internal class SimApiClientImpl<T : SimRemoteInterface>(
    private val service: KClass<T>,
    private val okHttpClientBuilder: DefaultOkHttpClientBuilder,
    private val url: String,
    private val deviceId: String,
    private val versionName: String,
    private val authToken: String? = null,
    private val attempts: Int = ATTEMPTS_FOR_NETWORK_CALLS,
) : SimNetwork.SimApiClient<T> {
    companion object {
        private const val BACKEND_MAINTENANCE_ERROR_STRING = "002"
        private const val HEADER_RETRY_AFTER = "retry-after"
        private val HTTP_CODES_FOR_RETRYABLE_ERROR = listOf(500, 502, 503)
        private const val ATTEMPTS_FOR_NETWORK_CALLS = 5
    }

    override val api: T by lazy {
        retrofit.create(service.java)
    }

    private val retrofit: Retrofit by lazy {
        Retrofit
            .Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(JacksonConverterFactory.create(JsonHelper.jackson))
            .baseUrl(url)
            .client(okHttpClientConfig.build())
            .build()
    }

    private val okHttpClientConfig: OkHttpClient.Builder by lazy {
        okHttpClientBuilder.get(authToken, deviceId, versionName)
    }

    override suspend fun <V> executeCall(networkBlock: suspend (T) -> V): V = try {
        retryIO(
            times = attempts,
            runBlock = {
                return@retryIO try {
                    withContext(Dispatchers.IO) {
                        networkBlock(api)
                    }
                } catch (e: Exception) {
                    throw transformExceptionIfNeeded(e)
                }
            },
            retryIf = { it is RetryableCloudException },
        )
    } catch (e: Exception) {
        throw when (e) {
            is RetryableCloudException -> SyncCloudIntegrationException(cause = e.cause!!)
            else -> e
        }
    }

    private fun transformExceptionIfNeeded(e: Exception): Exception = when {
        e is HttpException -> {
            when {
                e.isBackendMaintenanceError() -> BackendMaintenanceException(
                    estimatedOutage = e.parseEstimatedOutage(),
                )
                HTTP_CODES_FOR_RETRYABLE_ERROR.contains(e.code()) -> RetryableCloudException(
                    cause = e,
                )
                else -> SyncCloudIntegrationException(cause = e)
            }
        }
        e.isCausedFromBadNetworkConnection() -> NetworkConnectionException(cause = e)
        else -> e
    }

    private fun HttpException.isBackendMaintenanceError(): Boolean {
        if (code() != 503) {
            return false
        }
        val apiError = response()?.errorBody()?.string()?.let { JsonHelper.fromJson<ApiError>(it) }
        return apiError?.error == BACKEND_MAINTENANCE_ERROR_STRING
    }

    private fun HttpException.parseEstimatedOutage(): Long? = try {
        response()?.headers()?.get(HEADER_RETRY_AFTER)?.toLong()
    } catch (e: NumberFormatException) {
        null
    }
}
