package com.simprints.id.activities.coreexitform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.db.session.eventdata.controllers.domain.SessionEventsManager

class CoreExitFormViewModelFactory(private val sessionEventsManager: SessionEventsManager) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(CoreExitFormViewModel::class.java)) {
            CoreExitFormViewModel(sessionEventsManager) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
