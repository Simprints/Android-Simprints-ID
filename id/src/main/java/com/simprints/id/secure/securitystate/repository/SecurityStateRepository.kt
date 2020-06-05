package com.simprints.id.secure.securitystate.repository

interface SecurityStateRepository {

    // TODO: replace Any with SecurityState once PAS-900 gets merged in
    suspend fun getSecurityState(): Any

}
