package com.simprints.id.secure.securitystate.repository

import com.simprints.id.secure.models.SecurityState
import com.simprints.id.secure.securitystate.local.SecurityStatusLocalDataSource
import com.simprints.id.secure.securitystate.remote.SecurityStateRemoteDataSource

class SecurityStateRepositoryImpl(
    private val remoteDataSource: SecurityStateRemoteDataSource,
    private val localDataSource: SecurityStatusLocalDataSource
) : SecurityStateRepository {

    override suspend fun getSecurityStateFromRemote(): SecurityState {
        return remoteDataSource.getSecurityState().also {
            localDataSource.updateSecurityStatus(it.status)
        }
    }

    override fun getSecurityStatusFromLocal(): SecurityState.Status
        = localDataSource.getSecurityStatus()

}
