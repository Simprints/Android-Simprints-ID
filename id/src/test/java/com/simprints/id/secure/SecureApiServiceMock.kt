package com.simprints.id.secure

import com.simprints.id.secure.models.*
import com.simprints.id.secure.models.remote.ApiAuthenticationData
import com.simprints.id.secure.models.remote.ApiToken
import com.simprints.testtools.common.retrofit.createMockBehaviorService
import io.reactivex.Single
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.mock.BehaviorDelegate
import retrofit2.mock.Calls

// It's required to use NetworkBehavior, even if response is not used in the tests (e.g failing modalityResponses due to no connectivity).
// To mock response (code, body, type) use FakeResponseInterceptor for okHttpClient
class SecureApiServiceMock(private val delegate: BehaviorDelegate<SecureApiInterface>) : SecureApiInterface {

    override fun requestPublicKey(projectId: String, userId: String, key: String): Single<Response<PublicKeyString>> {
        val publicKeyResponse = PublicKeyString("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCAmxhSp1nSNOkRianJtMEP6uEznURRKeLmnr5q/KJnMosVeSHCtFlsDeNrjaR9r90sUgn1oA++ixcu3h6sG4nq4BEgDHi0aHQnZrFNq+frd002ji5sb9dUM2n6M7z8PPjMNiy7xl//qDIbSuwMz9u5G1VjovE4Ej0E9x1HLmXHRQIDAQAB")
        return delegate.returning(buildSuccessResponseWith(publicKeyResponse)).requestPublicKey(projectId, userId, key)
    }

    override fun requestNonce(projectId: String, userId: String, key: String): Single<Response<Nonce>> {
        val nonceResponse = Nonce("nonce_from_server")
        return delegate.returning(buildSuccessResponseWith(nonceResponse)).requestNonce(projectId, userId)
    }

    override fun requestAuthenticationData(projectId: String, userId: String, key: String): Single<Response<ApiAuthenticationData>> {
        val authData = ApiAuthenticationData("nonce_from_server",
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCAmxhSp1nSNOkRianJtMEP6uEznURRKeLmnr5q/KJnMosVeSHCtFlsDeNrjaR9r90sUgn1oA++ixcu3h6sG4nq4BEgDHi0aHQnZrFNq+frd002ji5sb9dUM2n6M7z8PPjMNiy7xl//qDIbSuwMz9u5G1VjovE4Ej0E9x1HLmXHRQIDAQAB")
        return delegate.returning(buildSuccessResponseWith(authData)).requestAuthenticationData(projectId, userId, key)
    }


    override fun requestCustomTokens(projectId: String, userId: String, credentials: AuthRequestBody, key: String): Single<Response<ApiToken>> {
        val tokens = ApiToken("legacy_token")
        return delegate.returning(buildSuccessResponseWith(tokens)).requestCustomTokens(projectId, userId, credentials)
    }

    private fun <T> buildSuccessResponseWith(body: T?): Call<T> {
        return Calls.response(Response.success(body))
    }
}

fun createMockServiceToFailRequests(retrofit: Retrofit): SecureApiInterface {
    return SecureApiServiceMock(createMockBehaviorService(retrofit, 100, SecureApiInterface::class.java))
}
