package com.simprints.id.secure.securitystate.remote

import com.simprints.core.login.LoginInfoManager
import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.secure.SecureApiInterface
import com.simprints.id.secure.models.SecurityState
import com.simprints.id.secure.models.remote.fromApiToDomain
import com.simprints.infra.network.SimApiClient

class SecurityStateRemoteDataSourceImpl(
    private val simApiClientFactory: SimApiClientFactory,
    private val loginInfoManager: LoginInfoManager,
    private val deviceId: String
) : SecurityStateRemoteDataSource {

    override suspend fun getSecurityState(): SecurityState {
        val projectId = loginInfoManager.getSignedInProjectIdOrEmpty()

        return getClient().executeCall {
            it.requestSecurityState(projectId, deviceId)
        }.fromApiToDomain()
    }

    private suspend fun getClient(): SimApiClient<SecureApiInterface> {
        return simApiClientFactory.buildClient(SecureApiInterface::class)
    }
}
