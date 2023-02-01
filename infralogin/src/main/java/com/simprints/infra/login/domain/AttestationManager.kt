package com.simprints.infra.login.domain

internal interface AttestationManager {

    suspend fun requestAttestation(nonce: String): String
}
