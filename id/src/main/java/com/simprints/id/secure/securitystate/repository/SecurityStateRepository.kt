package com.simprints.id.secure.securitystate.repository

import com.simprints.id.secure.models.SecurityState

interface SecurityStateRepository {


    suspend fun getSecurityStatusFromRemote(): SecurityState

    fun getSecurityStatusFromLocal(): SecurityState.Status

}
