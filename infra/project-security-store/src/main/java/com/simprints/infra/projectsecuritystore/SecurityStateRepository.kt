package com.simprints.infra.projectsecuritystore

import com.simprints.infra.projectsecuritystore.securitystate.models.SecurityState

interface SecurityStateRepository {
    suspend fun getSecurityStatusFromRemote(): SecurityState
    fun getSecurityStatusFromLocal(): SecurityState.Status
}
