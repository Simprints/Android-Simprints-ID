package com.simprints.id.activities.faceexitform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.db.event.EventRepository

class FaceExitFormViewModelFactory(private val eventRepository: EventRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(FaceExitFormViewModel::class.java)) {
            FaceExitFormViewModel(eventRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
