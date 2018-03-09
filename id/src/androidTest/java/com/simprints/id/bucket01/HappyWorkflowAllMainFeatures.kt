package com.simprints.id.bucket01


import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.simprints.id.activities.launch.LaunchActivity
import com.simprints.id.templates.FirstUseTest
import com.simprints.id.templates.HappyBluetooth
import com.simprints.id.templates.HappyWifi
import com.simprints.id.testHappyWorkflowEnrolment
import com.simprints.id.testHappyWorkflowIdentification
import com.simprints.id.testHappyWorkflowVerification
import com.simprints.id.tools.CalloutCredentials
import com.simprints.id.tools.log
import com.simprints.id.data.db.local.RealmConfig
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
class HappyWorkflowAllMainFeatures : FirstUseTest(), HappyWifi, HappyBluetooth {

    override val calloutCredentials: CalloutCredentials = CalloutCredentials(
        "00000001-0000-0000-0000-000000000000",
        "the_one_and_only_module",
        "the_lone_user")

    override var realmConfiguration: RealmConfiguration? = null

    @Rule
    @JvmField
    val enrolTestRule = ActivityTestRule(LaunchActivity::class.java, false, false)

    @Rule
    @JvmField
    val identifyTestRule = ActivityTestRule(LaunchActivity::class.java, false, false)

    @Rule
    @JvmField
    val verifyTestRule = ActivityTestRule(LaunchActivity::class.java, false, false)

    @Before
    @Throws(ApiException::class)
    override fun setUp() {
        log("bucket01.HappyWorkflowAllMainFeatures.setUp()")
        super<HappyWifi>.setUp()
        super<HappyBluetooth>.setUp()

        Realm.init(getInstrumentation().targetContext)
        realmConfiguration = (RealmConfig.get(calloutCredentials.apiKey))
        super<FirstUseTest>.setUp()
    }

    @Test
    fun happyWorkflowAllMainFeatures() {
        log("bucket01.HappyWorkflowAllMainFeatures.happyWorkflowAllMainFeatures")
        val guid = testHappyWorkflowEnrolment(calloutCredentials, enrolTestRule)
        testHappyWorkflowIdentification(calloutCredentials, identifyTestRule, guid)
        testHappyWorkflowVerification(calloutCredentials, verifyTestRule, guid)
    }

    @After
    override fun tearDown() {
        log("bucket01.HappyWorkflowAllMainFeatures.tearDown()")
        super<HappyBluetooth>.tearDown()
    }
}
