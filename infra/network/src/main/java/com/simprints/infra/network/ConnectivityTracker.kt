package com.simprints.infra.network

import kotlinx.coroutines.flow.Flow

interface ConnectivityTracker {
    fun observeIsConnected(): Flow<Boolean>

    fun isConnected(): Boolean
}
