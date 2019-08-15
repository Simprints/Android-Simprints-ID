package com.simprints.id.activities.consent

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.consent.shortconsent.ConsentDataManager
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintEnrolRequest
import javax.inject.Inject

class ConsentViewModelFactory @Inject constructor(private val consentDataManager: ConsentDataManager,
                                                  private val crashReportManager: CrashReportManager,
                                                  private val fingerprintRequest: FingerprintEnrolRequest,
                                                  private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ConsentViewModel::class.java)) {
            ConsentViewModel(consentDataManager, crashReportManager, fingerprintRequest, context) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }


}
