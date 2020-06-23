package com.simprints.id.secure.securitystate.local

import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.secure.models.SecurityState

class SecurityStateLocalDataSourceImpl(
    private val settingsPreferencesManager: SettingsPreferencesManager
) : SecurityStateLocalDataSource {

    override fun getSecurityStatus(): SecurityState.Status {
        return settingsPreferencesManager.securityStatus
    }

    override fun setSecurityStatus(securityStatus: SecurityState.Status) {
        settingsPreferencesManager.securityStatus = securityStatus
    }

}
