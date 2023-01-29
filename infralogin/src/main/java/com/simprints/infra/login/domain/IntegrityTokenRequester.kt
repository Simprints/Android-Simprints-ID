package com.simprints.infra.login.domain

internal interface IntegrityTokenRequester {

    fun getToken(nonce: String): String
}
