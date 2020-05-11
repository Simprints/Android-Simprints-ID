package com.simprints.id.network

import com.google.gson.Gson
import com.simprints.core.tools.json.JsonHelper

class SimApiClientFactory(
    val baseUrlProvider: BaseUrlProvider,
    val deviceId: String,
    val jsonAdapter: Gson = JsonHelper.gson
) {

    inline fun <reified T> build(
        authToken: String? = null
    ): SimApiClient<T> {
        return SimApiClient(
            T::class.java,
            baseUrlProvider.getApiBaseUrl(),
            deviceId,
            authToken,
            jsonAdapter
        )
    }

}
