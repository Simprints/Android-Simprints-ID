package com.simprints.feature.dashboard.sync

import androidx.lifecycle.LiveData

interface DeviceManager {

    val isConnectedLiveData: LiveData<Boolean>
}
