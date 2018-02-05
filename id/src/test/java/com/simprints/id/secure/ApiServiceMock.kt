package com.simprints.id.secure

import com.simprints.id.secure.models.Nonce
import com.simprints.id.secure.models.PublicKeyString
import com.simprints.id.tools.retrofit.buildResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Query
import retrofit2.mock.BehaviorDelegate
import retrofit2.mock.Calls


// It's required to use NetworkBehavior, even if response is not used in the tests (e.g failing responses due to no connectivity)
// To mock response (code, body, type) use FakeResponseInterceptor for okHttpClient
class ApiServiceMock(private val delegate: BehaviorDelegate<ApiServiceInterface>) : ApiServiceInterface {

    private val publicKeyResponse = buildResponse(200, "public_key_from_server")

    private val nonceResponse = buildResponse(200, "nonce_from_server")

    override fun publicKey(@Query("key") key: String): Single<PublicKeyString> {
        return delegate.returningResponse(Calls.response(Response.success(publicKeyResponse.body(), publicKeyResponse))).publicKey(key)
    }

    override fun nonce(headers: Map<String, String>, key: String): Single<Nonce> {
        return delegate.returningResponse(Calls.response(Response.success(nonceResponse.body(), nonceResponse))).nonce(headers, key)
    }
}
