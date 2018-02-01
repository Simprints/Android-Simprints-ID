package com.simprints.id.secure

import io.reactivex.Single

class AuthManager {

    companion object {
        fun requestAuthToken(request: AuthRequest): Single<String> {
            return Single.create<String> { emitter ->
                Network().execute({
                    val token = it
                    emitter.onSuccess(token)
                }, {
                    emitter.onError(it)
                })
            }
        }
    }
}
