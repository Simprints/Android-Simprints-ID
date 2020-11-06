package com.simprints.id.activities.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.db.event.EventRepository

class ConsentViewModelFactory(private val eventRepository: EventRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ConsentViewModel::class.java)) {
            ConsentViewModel(eventRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
