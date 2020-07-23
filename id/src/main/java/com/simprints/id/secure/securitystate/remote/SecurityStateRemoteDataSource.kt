package com.simprints.id.secure.securitystate.remote

import com.simprints.id.secure.models.SecurityState

interface SecurityStateRemoteDataSource {

    suspend fun getSecurityState(): SecurityState

}
