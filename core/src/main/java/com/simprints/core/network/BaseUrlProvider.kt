package com.simprints.core.network

interface BaseUrlProvider {
    fun getApiBaseUrl(): String
    fun setApiBaseUrl(apiBaseUrl: String?)
    fun resetApiBaseUrl()

    fun getImageStorageBucketUrl(): String
    fun setImageStorageBucketUrl(imageStorageBucketUrl: String)
    fun resetImageStorageBucketUrl()
}
