package com.simprints.infra.login.domain

internal interface AttestationManager {

    fun requestPlayIntegrityToken(nonce: String): String
}
