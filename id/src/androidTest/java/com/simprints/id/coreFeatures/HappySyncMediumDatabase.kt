package com.simprints.id.coreFeatures

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.simprints.id.activities.launch.LaunchActivity
import com.simprints.id.data.db.local.realm.RealmConfig
import com.simprints.id.testTemplates.FirstUseLocal
import com.simprints.id.testTemplates.FirstUseRemote
import com.simprints.id.testTemplates.HappyBluetooth
import com.simprints.id.testTemplates.HappyWifi
import com.simprints.id.testSnippets.testHappySync
import com.simprints.id.testTools.CalloutCredentials
import com.simprints.id.testTools.RemoteAdminUtils
import com.simprints.id.testTools.SyncParameters
import com.simprints.id.testTools.log
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
class HappySyncMediumDatabase : FirstUseLocal, FirstUseRemote, HappyWifi, HappyBluetooth {

    override val calloutCredentials: CalloutCredentials = CalloutCredentials(
        "00000002-0000-0000-0000-000000000000",
        "the_one_and_only_module",
        "the_lone_user")

    override var realmConfiguration: RealmConfiguration? = null

    @Rule
    @JvmField
    val identifyTestRule = ActivityTestRule(LaunchActivity::class.java, false, false)

    @Before
    @Throws(ApiException::class)
    override fun setUp() {
        log("bucket01.HappySyncMediumDatabase.setUp()")
        super<HappyWifi>.setUp()
        super<HappyBluetooth>.setUp()

        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        realmConfiguration = RealmConfig.get(calloutCredentials.projectId, byteArrayOf())
        super<FirstUseLocal>.setUp()
        super<FirstUseRemote>.setUp()

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
        super<HappyBluetooth>.tearDown()
    }
}
