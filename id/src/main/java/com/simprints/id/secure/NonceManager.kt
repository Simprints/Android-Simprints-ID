package com.simprints.id.secure

import io.reactivex.Single

class NonceManager {

    companion object {
        fun requestNonce(noneScope: NonceScope): Single<String> {
            return Single.create<String> { emitter ->
                Network().execute({
                    emitter.onSuccess(it)
                }, {
                    emitter.onError(it)
                })
            }
        }
    }
}
