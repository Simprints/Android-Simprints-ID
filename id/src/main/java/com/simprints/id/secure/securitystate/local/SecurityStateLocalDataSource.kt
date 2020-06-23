package com.simprints.id.secure.securitystate.local

import com.simprints.id.secure.models.SecurityState

interface SecurityStateLocalDataSource {

    fun getSecurityStatus(): SecurityState.Status

    fun setSecurityStatus(securityStatus: SecurityState.Status)

}
