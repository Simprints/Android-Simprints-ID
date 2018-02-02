package com.simprints.id.secure

import com.simprints.id.secure.domain.PublicKeyString
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class PublicKeyManager {

    fun requestPublicKey(): Single<PublicKeyString> =
        ApiService.publicKey("AIzaSyAORPo9YH-TBw0F1ch8BMP9IGkNElgon6s")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
}
