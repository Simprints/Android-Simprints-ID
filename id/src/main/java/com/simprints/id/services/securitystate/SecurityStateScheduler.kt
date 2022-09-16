package com.simprints.id.services.securitystate

interface SecurityStateScheduler {

    fun getSecurityStateCheck()
    fun scheduleSecurityStateCheck()
    fun cancelSecurityStateCheck()

}
