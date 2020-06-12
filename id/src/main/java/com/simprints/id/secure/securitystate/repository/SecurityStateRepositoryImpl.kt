package com.simprints.id.secure.securitystate.repository

import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.secure.models.SecurityState
import com.simprints.id.secure.securitystate.remote.SecurityStateRemoteDataSource

class SecurityStateRepositoryImpl(
    private val remoteDataSource: SecurityStateRemoteDataSource,
    private val settingsPreferencesManager: SettingsPreferencesManager
) : SecurityStateRepository {

    override suspend fun getSecurityState(): SecurityState {
        return remoteDataSource.getSecurityState().also {
            settingsPreferencesManager.securityStatus = it.status
        }
    }

}
