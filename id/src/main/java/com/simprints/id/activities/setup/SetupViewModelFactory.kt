package com.simprints.id.activities.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.tools.device.DeviceManager
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class SetupViewModelFactory(private val deviceManager: DeviceManager
): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SetupViewModel::class.java)) {
         SetupViewModel(deviceManager) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
