package com.simprints.id.activities.fetchguid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.device.DeviceManager

class FetchGuidViewModelFactory(private val personRepository: PersonRepository,
                                private val deviceManager: DeviceManager,
                                private val sessionRepository: SessionRepository,
                                private val timeHelper: TimeHelper) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(FetchGuidViewModel::class.java)) {
            FetchGuidViewModel(personRepository, deviceManager, sessionRepository, timeHelper) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }


}
