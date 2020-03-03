package com.simprints.core.network

import com.google.gson.Gson
import com.simprints.core.tools.json.JsonHelper

class SimApiClientFactory(val deviceId: String,
                          val endpoint: String = NetworkConstants.BASE_URL,
                          val jsonAdapter: Gson = JsonHelper.gson) {

    inline fun <reified T> build(authToken: String? = null): SimApiClient<T> =
        SimApiClient(T::class.java, endpoint, deviceId, authToken, jsonAdapter)
}
