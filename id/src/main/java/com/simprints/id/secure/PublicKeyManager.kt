package com.simprints.id.secure

import com.simprints.id.secure.cryptography.AsymmetricEncrypter
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class PublicKeyManager {

    fun requestPublicKey(): Single<String> =
        ApiService.publicKey("AIzaSyAORPo9YH-TBw0F1ch8BMP9IGkNElgon6s")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun encryptProjectSecret(projectSecret: String, publicKey: String): Single<String> {
        return Single.create<String> { emitter ->
            val encryptProjectSecret = AsymmetricEncrypter(publicKey).encrypt(projectSecret)
            emitter.onSuccess(encryptProjectSecret)
        }
    }
}
