package com.simprints.id.secure

import android.util.Base64
import org.json.JSONObject

class JwtTokenHelper {

    companion object {
        fun extractAsJson(jwtToken: String): JSONObject? {
            //the JWT (JSON WEB TOKEN) is just a 3 base64 encoded parts concatenated by a . character
            val jwtParts = extractingJWTParts(jwtToken)

            return if (jwtParts.size == 3) {
                //we're only really interested in the body/payload
                val decodedPayload = String(Base64.decode(jwtParts[1], Base64.DEFAULT))

                JSONObject(decodedPayload)
            } else {
                null
            }
        }

        private fun extractingJWTParts(jwsResult: String) =
            jwsResult.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    }
}
