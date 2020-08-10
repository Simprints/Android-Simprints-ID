package com.simprints.id.activities.settings.fingerselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.prefs.PreferencesManager

class FingerSelectionViewModelFactory(private val preferencesManager: PreferencesManager,
                                      private val crashReportManager: CrashReportManager) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(FingerSelectionViewModel::class.java)) {
            FingerSelectionViewModel(preferencesManager, crashReportManager) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
