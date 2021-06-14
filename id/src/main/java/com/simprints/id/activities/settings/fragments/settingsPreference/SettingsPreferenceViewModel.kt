package com.simprints.id.activities.settings.fragments.settingsPreference

import androidx.lifecycle.ViewModel
import com.simprints.core.analytics.CrashReportManager
import com.simprints.core.analytics.CrashReportTag
import com.simprints.core.analytics.CrashReportTrigger

class SettingsPreferenceViewModel(
    private val crashReportManager: CrashReportManager
) : ViewModel() {

    fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(CrashReportTag.SETTINGS, CrashReportTrigger.UI, message = message)
    }
}
