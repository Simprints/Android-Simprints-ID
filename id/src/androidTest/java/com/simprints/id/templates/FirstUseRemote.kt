package com.simprints.id.templates

import com.simprints.id.tools.CalloutCredentials
import com.simprints.id.tools.RemoteAdminUtils
import com.simprints.id.tools.log
import com.simprints.remoteadminclient.ApiException
import org.junit.Before


interface FirstUseRemote {

    val calloutCredentials: CalloutCredentials

    @Before
    @Throws(ApiException::class)
    fun setUp() {
        // Clear the project for the test's APIkey via remote admin
        log("FirstUseTest.setUp(): cleaning remote data")
        val apiInstance = RemoteAdminUtils.configuredApiInstance
        RemoteAdminUtils.clearProjectNode(apiInstance, calloutCredentials.projectId)
    }
}
