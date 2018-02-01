package com.simprints.id.secure
import io.reactivex.Single

class PublicKeyManager {

    companion object {

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
                val encryptProjectSecret = publicKey + projectSecret + "encrypt"
                emitter.onSuccess(encryptProjectSecret)
            }
        }
    }
}
