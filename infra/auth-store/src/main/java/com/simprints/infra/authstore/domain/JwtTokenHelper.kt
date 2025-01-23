package com.simprints.infra.authstore.domain

import android.util.Base64
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.LOGIN
import com.simprints.infra.logging.Simber
import org.json.JSONObject

internal class JwtTokenHelper {
    companion object {
        fun extractTokenPayloadAsJson(jwtToken: String?): JSONObject? = try {
            // the JWT (JSON WEB TOKEN) is just a 3 base64 encoded parts concatenated by a . character
            val jwtParts = jwtToken?.let { extractingJWTParts(it) }

            if (jwtParts?.size == 3) {
                // we're only really interested in the body/payload
                val decodedPayload = String(Base64.decode(jwtParts[1], Base64.URL_SAFE))

                JSONObject(decodedPayload)
            } else {
                throw Throwable("Impossible to parse jwt")
            }
        } catch (t: Throwable) {
            Simber.e("Failed to parse JWT", t, tag = LOGIN)
            null
        }

        private fun extractingJWTParts(jwsResult: String) = jwsResult.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    }
}
