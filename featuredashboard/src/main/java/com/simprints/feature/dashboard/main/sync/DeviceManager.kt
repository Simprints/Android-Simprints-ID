package com.simprints.feature.dashboard.main.sync

import androidx.lifecycle.LiveData

interface DeviceManager {

    val isConnectedLiveData: LiveData<Boolean>
}
