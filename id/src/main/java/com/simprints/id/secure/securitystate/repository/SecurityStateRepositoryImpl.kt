package com.simprints.id.secure.securitystate.repository

import com.simprints.id.secure.models.SecurityState
import com.simprints.id.secure.securitystate.local.SecurityStateLocalDataSource
import com.simprints.id.secure.securitystate.remote.SecurityStateRemoteDataSource
import javax.inject.Inject

class SecurityStateRepositoryImpl @Inject constructor(
    private val remoteDataSource: SecurityStateRemoteDataSource,
    private val localDataSource: SecurityStateLocalDataSource
) : SecurityStateRepository {

    override suspend fun getSecurityStatusFromRemote(): SecurityState {
        return remoteDataSource.getSecurityState().also {
            localDataSource.securityStatus = it.status
        }
    }

    override fun getSecurityStatusFromLocal(): SecurityState.Status {
        return localDataSource.securityStatus
    }
}
