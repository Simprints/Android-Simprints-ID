package com.simprints.infra.network

import android.content.Context
import com.simprints.infra.network.apiclient.SimApiClientImpl
import com.simprints.infra.network.url.BaseUrlProvider
import javax.inject.Inject
import kotlin.reflect.KClass

internal class SimNetworkImpl @Inject constructor(private val baseUrlProvider: BaseUrlProvider) :
    SimNetwork {

    override fun <T : SimRemoteInterface> getSimApiClient(
        remoteInterface: KClass<T>,
        ctx: Context,
        url: String,
        deviceId: String,
        versionName: String,
        authToken: String?
    ): SimNetwork.SimApiClient<T> {
        return SimApiClientImpl(
            remoteInterface,
            ctx,
            url,
            deviceId,
            versionName,
            authToken
        )
    }

    override fun getApiBaseUrl(): String {
        return baseUrlProvider.getApiBaseUrl()
    }

    override fun setApiBaseUrl(apiBaseUrl: String?) {
        baseUrlProvider.setApiBaseUrl(apiBaseUrl)
    }

    override fun resetApiBaseUrl() {
        baseUrlProvider.resetApiBaseUrl()
    }

}
