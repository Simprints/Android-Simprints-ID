package com.simprints.id.activities.coreexitform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CoreExitFormViewModelFactory(private val eventRepository: com.simprints.eventsystem.event.EventRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(CoreExitFormViewModel::class.java)) {
            CoreExitFormViewModel(eventRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
