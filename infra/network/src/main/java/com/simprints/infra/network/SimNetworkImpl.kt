package com.simprints.infra.network

import com.simprints.infra.network.apiclient.SimApiClientImpl
import com.simprints.infra.network.httpclient.DefaultOkHttpClientBuilder
import com.simprints.infra.network.url.BaseUrlProvider
import javax.inject.Inject
import kotlin.reflect.KClass

internal class SimNetworkImpl @Inject constructor(
    private val baseUrlProvider: BaseUrlProvider,
    private val okHttpClientBuilder: DefaultOkHttpClientBuilder,
) : SimNetwork {
    override fun <T : SimRemoteInterface> getSimApiClient(
        remoteInterface: KClass<T>,
        deviceId: String,
        versionName: String,
        authToken: String?,
    ): SimNetwork.SimApiClient<T> = SimApiClientImpl(
        remoteInterface,
        okHttpClientBuilder,
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
