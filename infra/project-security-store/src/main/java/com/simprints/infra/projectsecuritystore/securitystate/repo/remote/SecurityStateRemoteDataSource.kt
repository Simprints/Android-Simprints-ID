package com.simprints.infra.projectsecuritystore.securitystate.repo.remote

import com.simprints.core.DeviceID
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.projectsecuritystore.securitystate.models.SecurityState
import javax.inject.Inject

internal class SecurityStateRemoteDataSource @Inject constructor(
    private val authStore: AuthStore,
    private val configRepository: ConfigRepository,
    @DeviceID private val deviceId: String
) {

    suspend fun getSecurityState(): SecurityState {
        val projectId = authStore.signedInProjectId
        val deviceConfiguration = configRepository.getDeviceConfiguration()

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
