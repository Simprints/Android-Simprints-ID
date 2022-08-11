package com.simprints.infra.network

import com.simprints.infra.network.url.BaseUrlProvider
import javax.inject.Inject

internal class SimNetworkImpl @Inject constructor(private val baseUrlProvider: BaseUrlProvider) :
    SimNetwork {

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
