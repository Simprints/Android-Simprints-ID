package com.simprints.id.activities.settings.fingerselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.infra.config.ConfigManager

class FingerSelectionViewModelFactory(private val configManager: ConfigManager) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(FingerSelectionViewModel::class.java)) {
            FingerSelectionViewModel(configManager) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
