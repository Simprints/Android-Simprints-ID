package com.simprints.infra.login.domain

internal interface PlayIntegrityTokenRequester {

    fun requestPlayIntegrityToken(nonce: String): String
}
