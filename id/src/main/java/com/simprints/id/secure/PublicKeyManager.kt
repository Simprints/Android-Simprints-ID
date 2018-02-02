package com.simprints.id.secure

import com.simprints.id.secure.cryptography.AsymmetricEncrypter
import io.reactivex.Single

class PublicKeyManager {

    companion object {
        private const val publicKeyGetUrl =
            "https://project-manager-dot-simprints-dev.appspot.com/public-key?key=AIzaSyAORPo9YH-TBw0F1ch8BMP9IGkNElgon6s"
    }

    fun requestPublicKey(): Single<String> {
        return Single.create<String> { emitter ->
            Network().execute({
                val publicKey = it
                emitter.onSuccess(publicKey)
            }, {
                emitter.onError(it)
            })
        }
    }

    fun encryptProjectSecret(projectSecret: String, publicKey: String): Single<String> {
        return Single.create<String> { emitter ->
            val encryptProjectSecret = AsymmetricEncrypter(publicKey).encrypt(projectSecret)
            emitter.onSuccess(encryptProjectSecret)
        }
    }
}
