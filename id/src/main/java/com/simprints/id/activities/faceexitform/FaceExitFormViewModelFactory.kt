package com.simprints.id.activities.faceexitform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FaceExitFormViewModelFactory(private val eventRepository: com.simprints.eventsystem.event.EventRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(FaceExitFormViewModel::class.java)) {
            FaceExitFormViewModel(eventRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
