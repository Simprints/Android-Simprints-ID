package com.simprints.id.secure

import com.simprints.id.secure.models.PublicKeyString
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class PublicKeyManager(val client: ApiServiceInterface) {

    fun requestPublicKey(): Single<PublicKeyString> =
        client.publicKey().subscribeOn(Schedulers.io())
}
