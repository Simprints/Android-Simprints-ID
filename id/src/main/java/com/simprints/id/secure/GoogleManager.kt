package com.simprints.id.secure

import io.reactivex.Single
import org.json.JSONObject

class GoogleManager {

    companion object {
        fun requestAttestation(nonce: String): Single<JSONObject> {
            return Single.create<JSONObject> { emitter ->
                Network().execute({
                    emitter.onSuccess(JSONObject("""{"payload":"all good"}"""))
                }, {
                    emitter.onError(it)
                })
            }
        }
    }
}
