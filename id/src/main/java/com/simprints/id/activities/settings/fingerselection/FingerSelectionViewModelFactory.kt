package com.simprints.id.activities.settings.fingerselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FingerSelectionViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(FingerSelectionViewModel::class.java)) {
            FingerSelectionViewModel() as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
