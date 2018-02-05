package com.simprints.id.secure

import com.simprints.id.secure.models.AuthRequest
import io.reactivex.Single

//Temporary to "simulate" an async network for auth - it will be properly implemented soon
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
