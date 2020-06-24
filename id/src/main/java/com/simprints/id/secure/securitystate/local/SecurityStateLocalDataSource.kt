package com.simprints.id.secure.securitystate.local

import com.simprints.id.secure.models.SecurityState

interface SecurityStateLocalDataSource {

    var securityStatus: SecurityState.Status

}
