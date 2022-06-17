package com.simprints.id.activities.fetchguid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.core.tools.time.TimeHelper
import com.simprints.id.tools.device.DeviceManager

class FetchGuidViewModelFactory(private val fetchGuidHelper: FetchGuidHelper,
                                private val deviceManager: DeviceManager,
                                private val eventRepository: com.simprints.eventsystem.event.EventRepository,
                                private val timeHelper: TimeHelper,
                                private val dispatcher: DispatcherProvider
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(FetchGuidViewModel::class.java)) {
            FetchGuidViewModel(fetchGuidHelper, deviceManager, eventRepository, timeHelper, dispatcher) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
