package com.simprints.core.network

import com.google.gson.Gson
import com.simprints.core.tools.json.JsonHelper

class SimApiClientFactory(val deviceId: String, val jsonAdapter: Gson = JsonHelper.gson) {

    inline fun <reified T> build(
        baseUrl: String,
        authToken: String? = null
    ): SimApiClient<T> {
        return SimApiClient(
            T::class.java,
            baseUrl,
            deviceId,
            authToken,
            jsonAdapter
        )
    }

}
