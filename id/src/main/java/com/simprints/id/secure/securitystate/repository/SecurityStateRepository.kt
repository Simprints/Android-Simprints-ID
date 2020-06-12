package com.simprints.id.secure.securitystate.repository

import com.simprints.id.secure.models.SecurityState
import kotlinx.coroutines.channels.Channel

interface SecurityStateRepository {

    val securityStatusChannel: Channel<SecurityState.Status>

    suspend fun getSecurityStateFromRemote(): SecurityState

}
