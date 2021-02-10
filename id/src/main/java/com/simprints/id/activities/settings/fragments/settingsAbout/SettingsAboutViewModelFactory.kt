package com.simprints.id.activities.settings.fragments.settingsAbout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManager
import com.simprints.id.secure.SignerManager

class SettingsAboutViewModelFactory(private val preferencesManager: PreferencesManager,
                                    private val signerManager: SignerManager,
                                    private val recentEventsManager: RecentEventsPreferencesManager)
    : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsAboutViewModel::class.java))
            return SettingsAboutViewModel(preferencesManager, signerManager, recentEventsManager) as T
        else
            throw IllegalArgumentException("View model not found")
    }
}
