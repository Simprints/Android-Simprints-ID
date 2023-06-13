package com.simprints.infra.projectsecurity.securitystate.repo

import com.simprints.infra.projectsecurity.securitystate.models.SecurityState
import com.simprints.infra.projectsecurity.securitystate.repo.local.SecurityStateLocalDataSource
import com.simprints.infra.projectsecurity.securitystate.repo.remote.SecurityStateRemoteDataSource
import javax.inject.Inject

internal class SecurityStateRepositoryImpl @Inject constructor(
    private val remoteDataSource: SecurityStateRemoteDataSource,
    private val localDataSource: SecurityStateLocalDataSource
) : SecurityStateRepository {

    override suspend fun getSecurityStatusFromRemote(): SecurityState = remoteDataSource
        .getSecurityState()
        .also { localDataSource.securityStatus = it.status }

    override fun getSecurityStatusFromLocal(): SecurityState.Status = localDataSource.securityStatus
}
