package com.simprints.infra.backendapi

import com.simprints.core.DeviceID
import com.simprints.core.PackageVersionName
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.SimRemoteInterface
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.KClass

/**
 * Centralized API client provider for backend API interfaces.
 * This is the single entry point for obtaining API clients in the infra layer.
 */
@Singleton
class BackendApiClient @Inject constructor(
    private val simNetwork: SimNetwork,
    private val authStore: AuthStore,
    @param:DeviceID private val deviceId: String,
    @param:PackageVersionName private val versionName: String,
) {
    /**
     * Executes an authenticated backend call for the given [remoteInterface], using the current Firebase token.
     *
     * @return [ApiResult.Success] with the value on success, or [ApiResult.Failure] if any exception is thrown.
     */
    suspend fun <T : SimRemoteInterface, V> executeCall(
        remoteInterface: KClass<T>,
        block: suspend (T) -> V,
    ): ApiResult<V> = try {
        wrapInApiResponse(getApiClient(remoteInterface, authStore.getFirebaseToken()).executeCall(block))
    } catch (t: Throwable) {
        wrapException(t)
    }

    /**
     * Executes an unauthenticated backend call for the given [remoteInterface].
     *
     * @return [ApiResult.Success] with the value on success, or [ApiResult.Failure] if any exception is thrown.
     */
    suspend fun <T : SimRemoteInterface, V> executeUnauthenticatedCall(
        remoteInterface: KClass<T>,
        block: suspend (T) -> V,
    ): ApiResult<V> = try {
        wrapInApiResponse(getApiClient(remoteInterface, null).executeCall(block))
    } catch (t: Throwable) {
        wrapException(t)
    }

    private suspend fun <T : SimRemoteInterface> getApiClient(
        remoteInterface: KClass<T>,
        authToken: String?,
    ): SimNetwork.SimApiClient<T> = simNetwork.getSimApiClient(remoteInterface, deviceId, versionName, authToken)

    private fun <V> wrapInApiResponse(data: V): ApiResult<V> = if (data is Response<*>) {
        // In cases where requests meta-data is required, the API client will return a Response object,
        // such requests will return the Response with the error code instead of throwing exception,
        // so we need to check the request was successful manually and wrap it in ApiResult accordingly.
        if (data.isSuccessful) {
            ApiResult.Success(data)
        } else {
            wrapException(SyncCloudIntegrationException(cause = HttpException(data)))
        }
    } else {
        // Non-response types will throw directly
        ApiResult.Success(data)
    }

    private fun <V> wrapException(t: Throwable): ApiResult.Failure<V> = when (t) {
        is CancellationException -> throw t // Maintain the coroutine control flow by rethrowing the cancellation exception
        else -> ApiResult.Failure(t)
    }
}
