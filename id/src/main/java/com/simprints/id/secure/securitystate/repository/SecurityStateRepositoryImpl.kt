package com.simprints.id.secure.securitystate.repository

import com.simprints.id.secure.models.SecurityState
import com.simprints.id.secure.securitystate.local.SecurityStateLocalDataSource
import com.simprints.id.secure.securitystate.remote.SecurityStateRemoteDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class SecurityStateRepositoryImpl(
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

    private suspend fun Channel<SecurityState.Status>.update(status: SecurityState.Status) {
        if (!isClosedForSend)
            send(status)
    }

}
