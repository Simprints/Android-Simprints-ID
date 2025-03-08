package com.simprints.infra.network

import com.simprints.infra.network.apiclient.SimApiClientImpl
import com.simprints.infra.network.httpclient.BuildOkHttpClientUseCase
import com.simprints.infra.network.url.BaseUrlProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

@Singleton
internal class SimNetworkImpl @Inject constructor(
    private val baseUrlProvider: BaseUrlProvider,
    private val buildOkHttpClient: BuildOkHttpClientUseCase,
) : SimNetwork {
    override fun <T : SimRemoteInterface> getSimApiClient(
        remoteInterface: KClass<T>,
        deviceId: String,
        versionName: String,
        authToken: String?,
    ): SimNetwork.SimApiClient<T> = SimApiClientImpl(
        remoteInterface,
        buildOkHttpClient,
        getApiBaseUrl(),
        deviceId,
        versionName,
        authToken,
    )

    override fun getApiBaseUrl(): String = baseUrlProvider.getApiBaseUrl()

    override fun getApiBaseUrlPrefix(): String = baseUrlProvider.getApiBaseUrlPrefix()

    override fun setApiBaseUrl(apiBaseUrl: String?) {
        baseUrlProvider.setApiBaseUrl(apiBaseUrl)
    }

    override fun resetApiBaseUrl() {
        baseUrlProvider.resetApiBaseUrl()
    }
}
