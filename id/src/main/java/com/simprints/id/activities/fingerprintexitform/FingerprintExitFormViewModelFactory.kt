package com.simprints.id.activities.fingerprintexitform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.db.event.EventRepository

class FingerprintExitFormViewModelFactory(private val eventRepository: EventRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(FingerprintExitFormViewModel::class.java)) {
            FingerprintExitFormViewModel(eventRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
