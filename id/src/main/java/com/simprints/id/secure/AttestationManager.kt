package com.simprints.id.secure

import android.util.Base64
import android.util.Base64.NO_WRAP
import com.auth0.jwt.JWT
import com.google.android.gms.safetynet.SafetyNetApi
import com.google.android.gms.safetynet.SafetyNetClient
import com.google.android.gms.tasks.Tasks
import com.simprints.id.BuildConfig
import com.simprints.id.exceptions.safe.secure.SafetyNetException
import com.simprints.id.exceptions.safe.secure.SafetyNetExceptionReason.INVALID_CLAIMS
import com.simprints.id.exceptions.safe.secure.SafetyNetExceptionReason.SERVICE_UNAVAILABLE
import com.simprints.id.secure.models.AttestToken
import com.simprints.id.secure.models.Nonce
import io.reactivex.Single

class AttestationManager {

    fun requestAttestation(safetyNetClient: SafetyNetClient, nonce: Nonce): Single<AttestToken> {
        return Single.fromCallable<AttestToken> {

            val result = getSafetyNetAttestationResponse(safetyNetClient, nonce)

            result.let {
                checkForErrorClaimAndThrow(it.jwsResult)
                AttestToken(it.jwsResult)
            }
        }
    }

    internal fun getSafetyNetAttestationResponse(safetyNetClient: SafetyNetClient, nonce: Nonce): SafetyNetApi.AttestationResponse {
        return try {
            Tasks.await(safetyNetClient.attest(Base64.decode(nonce.value, NO_WRAP), BuildConfig.ANDROID_AUTH_API_KEY))
        } catch (e: Throwable) {
            throw SafetyNetException(reason = SERVICE_UNAVAILABLE)
        }
    }

    private fun checkForErrorClaimAndThrow(jwsResult: String?) {
        if (JWT.decode(jwsResult).claims.containsKey("error")) {
            throw SafetyNetException(reason = INVALID_CLAIMS)
        }
    }
}
