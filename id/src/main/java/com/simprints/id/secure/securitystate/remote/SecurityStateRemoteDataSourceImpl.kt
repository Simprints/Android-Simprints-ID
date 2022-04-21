package com.simprints.id.secure.securitystate.remote

import com.simprints.core.login.LoginInfoManager
import com.simprints.core.network.SimApiClient
import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.exceptions.safe.SimprintsInternalServerException
import com.simprints.id.secure.SecureApiInterface
import com.simprints.id.secure.models.SecurityState
import retrofit2.HttpException
import retrofit2.Response

class SecurityStateRemoteDataSourceImpl(
    private val simApiClientFactory: SimApiClientFactory,
    private val loginInfoManager: LoginInfoManager,
    private val deviceId: String
) : SecurityStateRemoteDataSource {

    override suspend fun getSecurityState(): SecurityState {
        val projectId = loginInfoManager.getSignedInProjectIdOrEmpty()

        val response = getClient().executeCall("requestSecurityState") {
            it.requestSecurityState(projectId, deviceId)
        }

        response.body()?.let {
            return it
        } ?: handleErrorResponse(response)
    }

    private suspend fun getClient(): SimApiClient<SecureApiInterface> {
        return simApiClientFactory.buildClient(SecureApiInterface::class)
    }

    private fun handleErrorResponse(response: Response<SecurityState>): Nothing {
        when (response.code()) {
            in 500..599 -> throw SimprintsInternalServerException()
            else -> throw HttpException(response)
        }
    }

}
