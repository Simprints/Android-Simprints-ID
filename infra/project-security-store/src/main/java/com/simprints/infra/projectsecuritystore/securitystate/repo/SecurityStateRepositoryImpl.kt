package com.simprints.infra.projectsecuritystore.securitystate.repo

import com.simprints.infra.projectsecuritystore.SecurityStateRepository
import com.simprints.infra.projectsecuritystore.securitystate.models.SecurityState
import com.simprints.infra.projectsecuritystore.securitystate.repo.local.SecurityStateLocalDataSource
import com.simprints.infra.projectsecuritystore.securitystate.repo.remote.SecurityStateRemoteDataSource
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
