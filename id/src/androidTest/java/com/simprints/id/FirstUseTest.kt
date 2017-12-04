package com.simprints.id

import android.Manifest.permission
import android.support.test.InstrumentationRegistry
import android.util.Log
import com.schibsted.spain.barista.permission.PermissionGranter
import com.simprints.remoteadminclient.ApiException
import io.realm.RealmConfiguration
import org.junit.After
import org.junit.Before
import java.util.*

open class FirstUseTest {

    private val permissions = ArrayList(Arrays.asList(
            permission.ACCESS_NETWORK_STATE,
            permission.BLUETOOTH,
            permission.INTERNET,
            permission.ACCESS_FINE_LOCATION,
            permission.RECEIVE_BOOT_COMPLETED,
            permission.WAKE_LOCK,
            permission.VIBRATE
    ))

    private var realmConfiguration: RealmConfiguration? = null
    private var apiKey: String? = null

    protected fun setRealmConfiguration(realmConfiguration: RealmConfiguration) {
        this.realmConfiguration = realmConfiguration
    }

    protected fun setApiKey(apiKey: String) {
        this.apiKey = apiKey
    }

    @Before
    @Throws(ApiException::class)
    open fun setUp() {
        Log.d("EndToEndTests", "FirstUseTest.setUp(): cleaning app data")

        // Clear any internal data
        StorageUtils.clearApplicationData(InstrumentationRegistry.getContext(), realmConfiguration!!)

        // Clear the project for the test's APIkey via remote admin
        val apiInstance = RemoteAdminUtils.configuredApiInstance
        RemoteAdminUtils.clearProjectNode(apiInstance, apiKey!!)

        // Allow all first-app permissions and dismiss the dialog box
        for (permission in permissions) PermissionGranter.allowPermissionsIfNeeded(permission)
    }

    @After
    open fun tearDown() {
        Log.d("EndToEndTests", "FirstUseTest.tearDown(): nothing")
    }
}
