package com.simprints.id.secure.securitystate.remote

import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.secure.SecureApiInterface
import com.simprints.id.secure.models.SecurityState
import com.simprints.id.secure.models.remote.fromApiToDomain
import com.simprints.infra.login.LoginManager
import com.simprints.infra.network.SimNetwork

class SecurityStateRemoteDataSourceImpl(
    private val loginManager: LoginManager,
    private val settingsPreferencesManager: SettingsPreferencesManager,
    private val deviceId: String
) : SecurityStateRemoteDataSource {

    override suspend fun getSecurityState(): SecurityState {
        val projectId = loginManager.getSignedInProjectIdOrEmpty()

        return getClient().executeCall {
            it.requestSecurityState(
                projectId,
                deviceId,
                settingsPreferencesManager.lastInstructionId
            )
        }.fromApiToDomain()
    }

    private suspend fun getClient(): SimNetwork.SimApiClient<SecureApiInterface> {
        return loginManager.buildClient(SecureApiInterface::class)
    }
}
