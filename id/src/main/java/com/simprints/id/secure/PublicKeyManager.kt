package com.simprints.id.secure

import com.simprints.id.secure.domain.PublicKeyString
import io.reactivex.Single

class PublicKeyManager {

    companion object {

        fun requestPublicKey(): Single<PublicKeyString> =
            ApiService.publicKey("AIzaSyAORPo9YH-TBw0F1ch8BMP9IGkNElgon6s")
    }
}
