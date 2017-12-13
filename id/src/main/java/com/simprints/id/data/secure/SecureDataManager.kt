package com.simprints.id.data.secure

interface SecureDataManager {

    var apiKey: String

    fun getApiKeyOr(default: String): String

}
