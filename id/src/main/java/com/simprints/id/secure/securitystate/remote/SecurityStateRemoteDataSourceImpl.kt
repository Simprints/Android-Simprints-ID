package com.simprints.id.secure.securitystate.remote

import com.simprints.id.secure.SecureApiInterface
import com.simprints.id.secure.models.SecurityState
import com.simprints.id.secure.models.remote.fromApiToDomain
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.login.LoginManager
import com.simprints.infra.network.SimNetwork

class SecurityStateRemoteDataSourceImpl(
    private val loginManager: LoginManager,
    private val configManager: ConfigManager,
    private val deviceId: String
) : SecurityStateRemoteDataSource {

    override suspend fun getSecurityState(): SecurityState {
        val projectId = loginManager.getSignedInProjectIdOrEmpty()
        val deviceConfiguration = configManager.getDeviceConfiguration()

        return getClient().executeCall {
            it.requestSecurityState(
                projectId,
                deviceId,
                deviceConfiguration.lastInstructionId
            )
        }.fromApiToDomain()
    }

    private suspend fun getClient(): SimNetwork.SimApiClient<SecureApiInterface> {
        return loginManager.buildClient(SecureApiInterface::class)
    }
}
