package com.simprints.id.secure.securitystate.remote

import com.simprints.core.DeviceID
import com.simprints.id.secure.SecureApiInterface
import com.simprints.id.secure.models.SecurityState
import com.simprints.id.secure.models.remote.fromApiToDomain
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.network.SimNetwork
import javax.inject.Inject

class SecurityStateRemoteDataSourceImpl @Inject constructor(
    private val authStore: AuthStore,
    private val configManager: ConfigManager,
    @DeviceID private val deviceId: String
) : SecurityStateRemoteDataSource {

    override suspend fun getSecurityState(): SecurityState {
        val projectId = authStore.signedInProjectId
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
        return authStore.buildClient(SecureApiInterface::class)
    }
}
