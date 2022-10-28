package com.simprints.id.activities.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.eventsystem.event.EventRepository
import com.simprints.infra.config.ConfigManager

class ConsentViewModelFactory(
    private val configManager: ConfigManager,
    private val eventRepository: EventRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ConsentViewModel::class.java)) {
            ConsentViewModel(configManager, eventRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }

}
