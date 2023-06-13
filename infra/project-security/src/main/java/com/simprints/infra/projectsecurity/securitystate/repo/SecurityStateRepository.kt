package com.simprints.infra.projectsecurity.securitystate.repo

import com.simprints.infra.projectsecurity.securitystate.models.SecurityState

interface SecurityStateRepository {
    suspend fun getSecurityStatusFromRemote(): SecurityState
    fun getSecurityStatusFromLocal(): SecurityState.Status
}
