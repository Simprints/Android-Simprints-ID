package com.simprints.id.activities.fetchguid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.device.DeviceManager

class FetchGuidViewModelFactory(private val downSyncHelper: EventDownSyncHelper,
                                private val deviceManager: DeviceManager,
                                private val eventRepository: EventRepository,
                                private val timeHelper: TimeHelper) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(FetchGuidViewModel::class.java)) {
            FetchGuidViewModel(downSyncHelper, deviceManager, eventRepository, timeHelper) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }


}
