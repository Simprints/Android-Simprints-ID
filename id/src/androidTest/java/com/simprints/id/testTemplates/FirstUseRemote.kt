package com.simprints.id.testTemplates

import com.simprints.id.testTools.CalloutCredentials
import com.simprints.id.testTools.RemoteAdminUtils
import com.simprints.id.testTools.log
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
