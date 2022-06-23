package com.simprints.id.secure

import android.util.Base64
import com.google.android.gms.safetynet.SafetyNetApi
import com.google.android.gms.safetynet.SafetyNetClient
import com.google.android.gms.tasks.Tasks
import com.simprints.id.BuildConfig
import com.simprints.id.exceptions.safe.secure.SafetyNetException
import com.simprints.id.exceptions.safe.secure.SafetyNetExceptionReason
import com.simprints.id.secure.models.AttestToken
import com.simprints.id.secure.models.Nonce
import com.simprints.infra.logging.Simber

class AttestationManagerImpl : AttestationManager {

    override fun requestAttestation(safetyNetClient: SafetyNetClient, nonce: Nonce): AttestToken {

        val result = getSafetyNetAttestationResponse(safetyNetClient, nonce)

        return result.let {
            checkForErrorClaimAndThrow(it.jwsResult)
            AttestToken(it.jwsResult!!)
        }
    }

    internal fun getSafetyNetAttestationResponse(
        safetyNetClient: SafetyNetClient,
        nonce: Nonce
    ): SafetyNetApi.AttestationResponse {
        return try {
            Tasks.await(safetyNetClient.attest(Base64.decode(nonce.value, Base64.NO_WRAP), BuildConfig.SAFETYNET_API_KEY))
        } catch (e: Throwable) {
            throw SafetyNetException(reason = SafetyNetExceptionReason.SERVICE_UNAVAILABLE)
        }
    }

    private fun checkForErrorClaimAndThrow(jwsResult: String?) {
        val response = JwtTokenHelper.extractTokenPayloadAsJson(jwsResult)

        Simber.tag("SafetyNet Response").i(response.toString())

        if (response?.has(SAFETYNET_TOKEN_ERROR_FILED) == true || response == null) {
            throw SafetyNetException(reason = SafetyNetExceptionReason.INVALID_CLAIMS)
        }
    }

    companion object {
        const val SAFETYNET_TOKEN_ERROR_FILED = "error"
    }
}
