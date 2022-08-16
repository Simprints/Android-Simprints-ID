package com.simprints.infra.login.domain

internal interface AttestationManager {

    fun requestAttestation(nonce: String): String
}
