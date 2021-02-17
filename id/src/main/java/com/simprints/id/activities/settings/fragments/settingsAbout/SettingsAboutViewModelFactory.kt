package com.simprints.id.activities.settings.fragments.settingsAbout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.secure.SignerManager

class SettingsAboutViewModelFactory(
    private val signerManager: SignerManager)
    : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SettingsAboutViewModel::class.java))
            SettingsAboutViewModel(signerManager) as T
        else
            throw IllegalArgumentException("View model not found")
    }
}
