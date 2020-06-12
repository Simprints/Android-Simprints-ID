package com.simprints.id.secure.securitystate.local

import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.secure.models.SecurityState

class SecurityStatusLocalDataSourceImpl(
    private val settingsPreferencesManager: SettingsPreferencesManager
) : SecurityStatusLocalDataSource {

    override fun getSecurityStatus(): SecurityState.Status =
        settingsPreferencesManager.securityStatus

    override fun updateSecurityStatus(securityStatus: SecurityState.Status) {
        settingsPreferencesManager.securityStatus = securityStatus
    }

}
