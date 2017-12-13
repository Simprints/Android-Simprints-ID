package com.simprints.libdata

interface SyncCallback {

    fun onInit()

    fun onStart(totalSize: Int)

    fun onProgress(remaining: Int)

    fun onFinish()

    fun onError(error: DATA_ERROR)

}
