package com.simprints.id.activities.fingerprintexitform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.db.session.SessionRepository

class FingerprintExitFormViewModelFactory(private val sessionRepository: SessionRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(FingerprintExitFormViewModel::class.java)) {
            FingerprintExitFormViewModel(sessionRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
