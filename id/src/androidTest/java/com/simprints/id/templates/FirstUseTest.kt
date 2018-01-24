package com.simprints.id.templates

import android.support.test.InstrumentationRegistry
import com.simprints.id.tools.CalloutCredentials
import com.simprints.id.tools.RemoteAdminUtils
import com.simprints.id.tools.StorageUtils
import com.simprints.id.tools.log
import com.simprints.remoteadminclient.ApiException
import io.realm.RealmConfiguration
import org.junit.Before

interface FirstUseTest {

    var realmConfiguration: RealmConfiguration?
    val calloutCredentials: CalloutCredentials

    @Before
    @Throws(ApiException::class)
    fun setUp() {
        log("FirstUseTest.setUp(): cleaning app data")

        // Clear any internal data
        log("FirstUseTest.setUp(): cleaning internal data")
        StorageUtils.clearApplicationData(InstrumentationRegistry.getContext(), realmConfiguration!!)

        // Clear the project for the test's APIkey via remote admin
        log("FirstUseTest.setUp(): cleaning remote data")
        val apiInstance = RemoteAdminUtils.configuredApiInstance
        RemoteAdminUtils.clearProjectNode(apiInstance, calloutCredentials.apiKey)
    }
}
