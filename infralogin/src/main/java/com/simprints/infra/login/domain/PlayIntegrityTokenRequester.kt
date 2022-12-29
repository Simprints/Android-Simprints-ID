package com.simprints.infra.login.domain

internal interface PlayIntegrityTokenRequester {

    fun getToken(nonce: String): String
}
