package com.simprints.id.secure.securitystate.repository

import com.simprints.id.secure.models.SecurityState
import com.simprints.id.secure.securitystate.remote.SecurityStateRemoteDataSource

class SecurityStateRepositoryImpl(
    private val remoteDataSource: SecurityStateRemoteDataSource
) : SecurityStateRepository {

    override suspend fun getSecurityState(): SecurityState {
        return remoteDataSource.getSecurityState()
    }

}
