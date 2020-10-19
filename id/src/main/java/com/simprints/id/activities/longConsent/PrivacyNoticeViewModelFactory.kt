package com.simprints.id.activities.longConsent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.prefs.PreferencesManager

class PrivacyNoticeViewModelFactory(
    private val longConsentRepository: LongConsentRepository,
    private val preferencesManager: PreferencesManager,
    private val dispatcherProvider: DispatcherProvider
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(PrivacyNoticeViewModel::class.java)) {
            PrivacyNoticeViewModel(longConsentRepository, preferencesManager.language, dispatcherProvider) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }

}
