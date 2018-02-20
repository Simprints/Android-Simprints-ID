package com.simprints.id.secure

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simprints.id.secure.models.Nonce
import com.simprints.id.secure.models.NonceScope
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class NonceManager(val client: ApiServiceInterface) {

    fun requestNonce(nonceScope: NonceScope): Single<Nonce> {
        val headers = convertNonceScopeIntoMap(nonceScope)
        return client.nonce(headers).subscribeOn(Schedulers.io())
    }

    private fun convertNonceScopeIntoMap(nonceScope: NonceScope): Map<String, String> {
        val jsonNonceScope = JsonHelper.toJson(nonceScope)
        val headersMapType = object : TypeToken<HashMap<String, String>>() {}.type
        return Gson().fromJson<HashMap<String, String>>(jsonNonceScope, headersMapType)
    }
}
