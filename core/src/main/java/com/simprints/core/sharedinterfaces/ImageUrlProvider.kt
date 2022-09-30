package com.simprints.core.sharedinterfaces

// TODO This interface is temporarily here so it can be used by the infraimages module until it is moved to the infraconfig module

interface ImageUrlProvider {
    suspend fun getImageStorageBucketUrl(): String?
}
