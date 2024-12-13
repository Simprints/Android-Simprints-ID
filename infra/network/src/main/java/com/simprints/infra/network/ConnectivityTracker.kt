package com.simprints.infra.network

import androidx.lifecycle.LiveData

interface ConnectivityTracker {
    fun observeIsConnected(): LiveData<Boolean>

    fun isConnected(): Boolean
}
