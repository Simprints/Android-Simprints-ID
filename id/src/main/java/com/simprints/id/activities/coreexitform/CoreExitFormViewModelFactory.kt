package com.simprints.id.activities.coreexitform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.db.session.SessionRepository

class CoreExitFormViewModelFactory(private val sessionRepository: SessionRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(CoreExitFormViewModel::class.java)) {
            CoreExitFormViewModel(sessionRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
