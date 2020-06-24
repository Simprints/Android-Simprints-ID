package com.simprints.id.secure.securitystate.remote

import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.network.SimApiClient
import com.simprints.id.network.SimApiClientFactory
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
        // Uncomment when testing
        // return SecurityState(deviceId, SecurityState.Status.RUNNING)
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
