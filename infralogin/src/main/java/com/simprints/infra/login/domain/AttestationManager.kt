package com.simprints.infra.login.domain

interface AttestationManager {

    fun requestAttestation(nonce: String): String
}
