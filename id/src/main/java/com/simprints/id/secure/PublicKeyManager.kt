package com.simprints.id.secure

import com.simprints.id.secure.models.PublicKeyString
import io.reactivex.Single

class PublicKeyManager(val client: ApiServiceInterface) {

    fun requestPublicKey(): Single<PublicKeyString> =
        client.publicKey("AIzaSyAORPo9YH-TBw0F1ch8BMP9IGkNElgon6s")
}
