package com.simprints.id.activities.settings.fragments.settingsAbout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.id.secure.SignerManager
import kotlinx.coroutines.launch

open class SettingsAboutViewModel(open val signerManager: SignerManager) : ViewModel() {

    open fun logout() {
        viewModelScope.launch { signerManager.signOut() }
    }
}
