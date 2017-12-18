package com.simprints.id.bucket01

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.simprints.id.activities.LaunchActivity
import com.simprints.id.templates.FirstUseTest
import com.simprints.id.testHappySync
import com.simprints.id.tools.CalloutCredentials
import com.simprints.id.tools.RemoteAdminUtils
import com.simprints.id.tools.SyncParameters
import com.simprints.id.tools.log
import com.simprints.libdata.models.realm.RealmConfig
import com.simprints.remoteadminclient.ApiException
import io.realm.Realm
import io.realm.RealmConfiguration
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class HappySyncMediumDatabase : FirstUseTest() {

    override val calloutCredentials: CalloutCredentials = CalloutCredentials(
            "00000002-0000-0000-0000-000000000000",
            "the_one_and_only_module",
            "the_lone_user")

    override var realmConfiguration: RealmConfiguration? = null

    @Rule @JvmField
    val identifyTestRule = ActivityTestRule(LaunchActivity::class.java, false, false)

    @Before
    @Throws(ApiException::class)
    override fun setUp() {
        log("bucket01.HappySyncMediumDatabase.setUp()")
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        realmConfiguration = RealmConfig.get(calloutCredentials.apiKey)
        super.setUp()

        log("bucket01.HappySyncMediumDatabase.setUp() creating remote database with ${SyncParameters.MEDIUM_DATABASE_NUMBER_OF_PATIENTS} patients")
        val apiInstance = RemoteAdminUtils.configuredApiInstance
        RemoteAdminUtils.createSimpleValidProject(apiInstance, calloutCredentials, SyncParameters.MEDIUM_DATABASE_NUMBER_OF_PATIENTS)
    }

    @Test
    fun happySyncMediumDatabase() {
        log("bucket01.HappySyncMediumDatabase.happySyncMediumDatabase")
        testHappySync(calloutCredentials, identifyTestRule)
    }

    @After
    override fun tearDown() {
        log("bucket01.HappySyncMediumDatabase.tearDown()")
        super.tearDown()
    }
}
