package com.simprints.infra.login.domain

internal interface IntegrityTokenRequester {

    suspend fun getToken(nonce: String): String
}
