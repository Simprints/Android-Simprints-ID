package com.simprints.id.secure

import com.simprints.id.secure.models.AuthRequestBody
import com.simprints.id.secure.models.remote.ApiAuthenticationData
import com.simprints.id.secure.models.remote.ApiToken
import com.simprints.testtools.common.retrofit.createMockBehaviorService
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.mock.BehaviorDelegate
import retrofit2.mock.Calls

// It's required to use NetworkBehavior, even if response is not used in the tests (e.g failing modalityResponses due to no connectivity).
// To mock response (code, body, type) use FakeResponseInterceptor for okHttpClient
class SecureApiServiceMock(private val delegate: BehaviorDelegate<SecureApiInterface>) : SecureApiInterface {

    override suspend fun requestAuthenticationData(projectId: String, userId: String, key: String): Response<ApiAuthenticationData> =
        delegate.returning(buildSuccessResponseWith(getApiAuthenticationData())).requestAuthenticationData(projectId, userId, key)

    override suspend fun requestCustomTokens(projectId: String, userId: String, credentials: AuthRequestBody, key: String): Response<ApiToken> =
        delegate.returning(buildSuccessResponseWith(getApiToken())).requestCustomTokens(projectId, userId, credentials)

    private fun getApiAuthenticationData() = ApiAuthenticationData("nonce_from_server",
        "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCAmxhSp1nSNOkRianJtMEP6uEznURRKeLmnr5q/KJnMosVeSHCtFlsDeNrjaR9r90sUgn1oA++ixcu3h6sG4nq4BEgDHi0aHQnZrFNq+frd002ji5sb9dUM2n6M7z8PPjMNiy7xl//qDIbSuwMz9u5G1VjovE4Ej0E9x1HLmXHRQIDAQAB")

    private fun <T> buildSuccessResponseWith(body: T?) = Calls.response(Response.success(body))

    private fun getApiToken() = ApiToken("legacy_token")
}

fun createMockServiceToFailRequests(retrofit: Retrofit): SecureApiInterface =
    SecureApiServiceMock(createMockBehaviorService(retrofit, 100, SecureApiInterface::class.java))

