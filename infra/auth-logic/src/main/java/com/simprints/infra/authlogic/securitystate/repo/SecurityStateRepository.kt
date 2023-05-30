package com.simprints.infra.authlogic.securitystate.repo

import com.simprints.infra.authlogic.securitystate.models.SecurityState
import com.simprints.infra.authlogic.securitystate.repo.local.SecurityStateLocalDataSource
import com.simprints.infra.authlogic.securitystate.repo.remote.SecurityStateRemoteDataSource
import javax.inject.Inject

internal class SecurityStateRepository @Inject constructor(
    private val remoteDataSource: SecurityStateRemoteDataSource,
    private val localDataSource: SecurityStateLocalDataSource
) {

    suspend fun getSecurityStatusFromRemote(): SecurityState = remoteDataSource
        .getSecurityState()
        .also { localDataSource.securityStatus = it.status }

    fun getSecurityStatusFromLocal(): SecurityState.Status = localDataSource.securityStatus
}
