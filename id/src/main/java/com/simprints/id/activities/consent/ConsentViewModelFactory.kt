package com.simprints.id.activities.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.eventsystem.event.EventRepository

class ConsentViewModelFactory(private val eventRepository: com.simprints.eventsystem.event.EventRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ConsentViewModel::class.java)) {
            ConsentViewModel(eventRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
