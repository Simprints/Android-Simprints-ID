package com.simprints.id.activities.settings.fragments.settingsAbout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.id.secure.SignerManager
import kotlinx.coroutines.launch

class SettingsAboutViewModel(val signerManager: SignerManager) : ViewModel() {

    fun logout() {
        viewModelScope.launch { signerManager.signOut() }
    }
}
