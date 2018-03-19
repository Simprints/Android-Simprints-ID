package com.simprints.id.secure

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simprints.id.exceptions.safe.secure.SimprintsInternalServerException
import com.simprints.id.secure.models.Nonce
import com.simprints.id.secure.models.NonceScope
import com.simprints.id.tools.JsonHelper
import com.simprints.id.tools.extensions.handleResponse
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

class NonceManager(val client: SecureApiInterface) {

    fun requestNonce(nonceScope: NonceScope): Single<Nonce> {
        val headers = convertNonceScopeIntoMap(nonceScope)

        return client.nonce(headers)
            .handleResponse(::handleResponseError)
            .subscribeOn(Schedulers.io())
    }

    private fun convertNonceScopeIntoMap(nonceScope: NonceScope): Map<String, String> {
        val jsonNonceScope = JsonHelper.toJson(nonceScope)
        val headersMapType = object : TypeToken<HashMap<String, String>>() {}.type
        return Gson().fromJson<HashMap<String, String>>(jsonNonceScope, headersMapType)
    }

    private fun handleResponseError(e: HttpException): Nothing =
        when (e.code()) {
            in 500..599 -> throw SimprintsInternalServerException()
            else -> throw e
        }
}
