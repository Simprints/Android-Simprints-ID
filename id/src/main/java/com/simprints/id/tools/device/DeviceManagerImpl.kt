package com.simprints.id.tools.device

import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.LiveData

class DeviceManagerImpl(private val context: Context) : DeviceManager {

    private val connectivityService  by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    override val isConnectedLiveData: LiveData<Boolean> by lazy {
        ConnectivityLiveData(connectivityService)
    }

    override suspend fun isConnected() = ConnectionCoroutine(connectivityService).isConnected()
}
