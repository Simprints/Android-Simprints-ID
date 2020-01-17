package com.simprints.id.tools.device

import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.LiveData
import com.scottyab.rootbeer.RootBeer
import com.simprints.id.exceptions.unexpected.RootedDeviceException



class DeviceManagerImpl(private val context: Context) : DeviceManager {

    private val connectivityService  by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    override val isConnectedUpdates: LiveData<Boolean> by lazy {
        ConnectivityLiveData(connectivityService)
    }

    override suspend fun isConnected() = ConnectionCoroutine(connectivityService).isConnected()

    override fun checkIfDeviceIsRooted() {
        val isDeviceRooted = RootBeer(context).isRootedWithoutBusyBoxCheck
        if (isDeviceRooted)
            throw RootedDeviceException()
    }
}
