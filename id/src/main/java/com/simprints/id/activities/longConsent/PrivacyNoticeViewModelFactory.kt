package com.simprints.id.activities.longConsent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.infra.config.ConfigManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class PrivacyNoticeViewModelFactory(
    private val longConsentRepository: LongConsentRepository,
    private val configManager: ConfigManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(PrivacyNoticeViewModel::class.java)) {
            PrivacyNoticeViewModel(longConsentRepository, configManager, dispatcher) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }

}
