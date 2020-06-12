package com.simprints.id.secure.securitystate.repository

import com.simprints.id.secure.models.SecurityState

interface SecurityStateRepository {

    suspend fun getSecurityStateFromRemote(): SecurityState

    fun getSecurityStatusFromLocal(): SecurityState.Status

}
