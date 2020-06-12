package com.simprints.id.secure.securitystate.local

import com.simprints.id.secure.models.SecurityState

interface SecurityStatusLocalDataSource {

    fun getSecurityStatus(): SecurityState.Status

    fun updateSecurityStatus(securityStatus: SecurityState.Status)

}
