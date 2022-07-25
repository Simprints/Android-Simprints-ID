package com.simprints.id.network

interface ImageUrlProvider {
    suspend fun getImageStorageBucketUrl(): String?
}
