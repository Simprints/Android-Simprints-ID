package com.simprints.infra.login.domain

import android.util.Base64
import com.google.android.gms.safetynet.SafetyNetApi
import com.google.android.gms.safetynet.SafetyNetClient
import com.google.android.gms.tasks.Tasks
import com.simprints.infra.logging.Simber
import com.simprints.infra.login.BuildConfig
import com.simprints.infra.login.exceptions.SafetyNetException
import javax.inject.Inject

internal class AttestationManagerImpl @Inject constructor(private val safetyNetClient: SafetyNetClient) : AttestationManager {

    override fun requestAttestation(nonce: String): String {
        val result = getSafetyNetAttestationResponse(safetyNetClient, nonce)

        return result.let {
            checkForErrorClaimAndThrow(it.jwsResult)
            it.jwsResult!!
        }
    }

    private fun getSafetyNetAttestationResponse(
        safetyNetClient: SafetyNetClient,
        nonce: String
    ): SafetyNetApi.AttestationResponse {
        return try {
            Tasks.await(
                safetyNetClient.attest(
                    Base64.decode(nonce, Base64.NO_WRAP),
                    BuildConfig.SAFETYNET_API_KEY
                )
            )
        } catch (e: Throwable) {
            throw SafetyNetException(reason = SafetyNetException.SafetyNetExceptionReason.SERVICE_UNAVAILABLE)
        }
    }

    private fun checkForErrorClaimAndThrow(jwsResult: String?) {
        val response = JwtTokenHelper.extractTokenPayloadAsJson(jwsResult)

        if (response == null || response.has(SAFETYNET_TOKEN_ERROR_FILED)) {
            Simber.tag("SafetyNet Response").i(response.toString())
            throw SafetyNetException(reason = SafetyNetException.SafetyNetExceptionReason.INVALID_CLAIMS)
        }
    }

    companion object {
        const val SAFETYNET_TOKEN_ERROR_FILED = "error"
    }
}
