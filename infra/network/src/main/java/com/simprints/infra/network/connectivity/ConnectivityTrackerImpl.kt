package com.simprints.infra.network.connectivity

import androidx.lifecycle.LiveData
import com.simprints.infra.network.ConnectivityTracker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ConnectivityTrackerImpl @Inject constructor(
    private val connectivityManagerWrapper: ConnectivityManagerWrapper,
) : ConnectivityTracker {
    private val isConnectedLiveData by lazy { ConnectivityLiveData(connectivityManagerWrapper) }

    override fun observeIsConnected(): LiveData<Boolean> = isConnectedLiveData

    override fun isConnected() = connectivityManagerWrapper.isNetworkAvailable()
}
