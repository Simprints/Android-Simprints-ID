package com.simprints.id.network

import com.simprints.core.exceptions.SyncCloudIntegrationException
import com.simprints.core.network.NetworkConstants
import com.simprints.core.network.SimApiClient
import com.simprints.core.network.SimRemoteInterface
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.core.tools.coroutines.retryIO
import com.simprints.core.tools.extentions.isBackendMaintenanceException
import com.simprints.core.tools.extentions.isCloudRecoverableIssue
import com.simprints.core.tools.json.JsonHelper
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import kotlin.reflect.KClass

open class SimApiClientImpl<T : SimRemoteInterface>(
    private val service: KClass<T>,
    private val url: String,
    private val deviceId: String,
    private val versionName: String,
    private val authToken: String? = null,
    private val jsonHelper: JsonHelper,
    private val dispatcher: DispatcherProvider,
    private val interceptor: Interceptor,
    private val okHttpClientBuilder: DefaultOkHttpClientBuilder = DefaultOkHttpClientBuilder()
) : SimApiClient<T> {

    override val api: T by lazy {
        retrofit.create(service.java)
    }

    open val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(JacksonConverterFactory.create(jsonHelper.jackson))
            .baseUrl(url)
            .client(okHttpClientConfig.build()).build()
    }

    val okHttpClientConfig: OkHttpClient.Builder by lazy {
        okHttpClientBuilder.get(authToken, deviceId, versionName,interceptor)
    }

    override suspend fun <V> executeCall(traceName: String?,
                                         networkBlock: suspend (T) -> V): V {

        return retryIO(
            times = NetworkConstants.RETRY_ATTEMPTS_FOR_NETWORK_CALLS,
            runBlock = {
                return@retryIO try {

                    withContext(dispatcher.io()) {
                        networkBlock(api)
                    }

                } catch (t: Throwable) {
                    val throwable = Throwable(t)
                    throw if (!t.isCloudRecoverableIssue()) {
                        SyncCloudIntegrationException("Http status code not worth to retry", throwable)
                    } else {
                        throwable
                    }
                }
            },
            retryIf = { it !is SyncCloudIntegrationException && !it.isBackendMaintenanceException() })
    }

}
