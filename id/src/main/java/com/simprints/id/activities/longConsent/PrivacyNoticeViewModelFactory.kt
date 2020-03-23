package com.simprints.id.activities.longConsent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.prefs.PreferencesManager

class PrivacyNoticeViewModelFactory(private val longConsentRepository: LongConsentRepository,
                                    private val preferencesManager: PreferencesManager): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(PrivacyNoticeViewModel::class.java)) {
            PrivacyNoticeViewModel(longConsentRepository, preferencesManager.language) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }

}
