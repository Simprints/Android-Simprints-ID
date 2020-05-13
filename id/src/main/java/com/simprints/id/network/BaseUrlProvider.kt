package com.simprints.id.network

interface BaseUrlProvider {
    fun getApiBaseUrl(): String
    fun setApiBaseUrl(apiBaseUrl: String?)
    fun resetApiBaseUrl()

    suspend fun getImageStorageBucketUrl(): String?
}
