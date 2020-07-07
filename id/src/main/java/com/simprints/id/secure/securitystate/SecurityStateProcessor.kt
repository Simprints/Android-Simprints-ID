package com.simprints.id.secure.securitystate

import com.simprints.id.secure.models.SecurityState

interface SecurityStateProcessor {

    suspend fun processSecurityState(securityState: SecurityState)

}
