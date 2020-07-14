package com.simprints.id.services.securitystate

interface SecurityStateScheduler {

    fun scheduleSecurityStateCheck()
    fun cancelSecurityStateCheck()

}
