package com.simprints.id.templates

import android.Manifest.permission
import android.support.test.InstrumentationRegistry
import com.schibsted.spain.barista.permission.PermissionGranter
import com.simprints.id.tools.CalloutCredentials
import com.simprints.id.tools.RemoteAdminUtils
import com.simprints.id.tools.StorageUtils
import com.simprints.id.tools.log
import com.simprints.remoteadminclient.ApiException
import io.realm.RealmConfiguration
import org.junit.After
import org.junit.Before
import java.util.*

abstract class FirstUseTest {

    private val permissions = ArrayList(Arrays.asList(
            permission.ACCESS_NETWORK_STATE,
            permission.BLUETOOTH,
            permission.INTERNET,
            permission.ACCESS_FINE_LOCATION,
            permission.RECEIVE_BOOT_COMPLETED,
            permission.WAKE_LOCK,
            permission.VIBRATE
    ))

    protected abstract var realmConfiguration: RealmConfiguration?
    protected abstract val calloutCredentials: CalloutCredentials

    @Before
    @Throws(ApiException::class)
    open fun setUp() {
        log("FirstUseTest.setUp(): cleaning app data")

        // Clear any internal data
        log("FirstUseTest.setUp(): cleaning internal data")
        StorageUtils.clearApplicationData(InstrumentationRegistry.getContext(), realmConfiguration!!)

        // Clear the project for the test's APIkey via remote admin
        log("FirstUseTest.setUp(): cleaning remote data")
        val apiInstance = RemoteAdminUtils.configuredApiInstance
        RemoteAdminUtils.clearProjectNode(apiInstance, calloutCredentials.apiKey)

        // Allow all first-app permissions and dismiss the dialog box
        log("FirstUseTest.setUp(): granting permissions")
        for (permission in permissions) PermissionGranter.allowPermissionsIfNeeded(permission)
    }

    @After
    open fun tearDown() {
        log("FirstUseTest.tearDown(): nothing")
    }
}
