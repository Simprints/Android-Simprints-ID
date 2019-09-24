package com.simprints.id.activities.fingerprintexitform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager

class FingerprintExitFormViewModelFactory(private val sessionEventsManager: SessionEventsManager) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(FingerprintExitFormViewModel::class.java)) {
            FingerprintExitFormViewModel(sessionEventsManager) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
