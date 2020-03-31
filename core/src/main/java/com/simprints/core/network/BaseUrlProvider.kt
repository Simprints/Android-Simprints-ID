package com.simprints.core.network

interface BaseUrlProvider {
    fun getApiBaseUrl(): String
    fun setApiBaseUrl(apiBaseUrl: String?)
    fun resetApiBaseUrl()

    suspend fun getImageStorageBucketUrl(): String?
}
