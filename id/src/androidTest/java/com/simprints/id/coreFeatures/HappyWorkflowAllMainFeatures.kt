package com.simprints.id.coreFeatures

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.util.Base64
import com.simprints.mockscanner.MockBluetoothAdapter
import com.simprints.mockscanner.MockFinger
import com.simprints.mockscanner.MockScannerManager
import com.simprints.id.Application
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.RealmConfig
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.di.AppModuleForAndroidTests
import com.simprints.id.di.DaggerForAndroidTests
import com.simprints.id.shared.DependencyRule.MockRule
import com.simprints.id.shared.DependencyRule.ReplaceRule
import com.simprints.id.testSnippets.*
import com.simprints.id.testTemplates.FirstUseLocal
import com.simprints.id.testTemplates.HappyWifi
import com.simprints.id.testTools.CalloutCredentials
import com.simprints.id.testTools.log
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.delegates.lazyVar
import io.realm.Realm
import io.realm.RealmConfiguration
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
class HappyWorkflowAllMainFeatures : DaggerForAndroidTests(), FirstUseLocal, HappyWifi {

    private val calloutCredentials = CalloutCredentials(
        "bWOFHInKA2YaQwrxZ7uJ",
        "the_one_and_only_module",
        "the_lone_user",
        "d95bacc0-7acb-4ff0-98b3-ae6ecbf7398f")

    private val realmKey = Base64.decode("Jk1P0NPgwjViIhnvrIZTN3eIpjWRrok5zBZUw1CiQGGWhTFgnANiS87J6asyTksjCHe4SHJo0dHeawAPz3JtgQ==", Base64.NO_WRAP)
    private val localDbKey = LocalDbKey(
        calloutCredentials.projectId,
        realmKey,
        calloutCredentials.legacyApiKey)

    private val projectSecret = "Z8nRspDoiQg1QpnDdKE6U7fQKa0GjpQOwnJ4OcSFWulAcIk4+LP9wrtDn8fRmqacLvkmtmOLl+Kxo1emXLsZ0Q=="

    override var realmConfiguration: RealmConfiguration? = null

    @Rule
    @JvmField
    val enrolTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    @Rule
    @JvmField
    val identifyTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    @Rule
    @JvmField
    val verifyTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    @Inject
    lateinit var remoteDbManager: RemoteDbManager
    @Inject
    lateinit var randomGeneratorMock: RandomGenerator

    override var module by lazyVar {
        AppModuleForAndroidTests(app,
            randomGeneratorRule = MockRule(),
            bluetoothComponentAdapterRule = ReplaceRule { mockBluetoothAdapter })
    }

    private lateinit var mockBluetoothAdapter: MockBluetoothAdapter

    @Before
    override fun setUp() {
        log("HappyWorkflowAllMainFeatures.setUp()")
        super<HappyWifi>.setUp()
        app = InstrumentationRegistry.getTargetContext().applicationContext as Application
        super<DaggerForAndroidTests>.setUp()
        testAppComponent.inject(this)

        setupRandomGeneratorToGenerateKey(realmKey, randomGeneratorMock)

        app.initDependencies()

        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        realmConfiguration = RealmConfig.get(localDbKey.projectId, localDbKey.value, localDbKey.projectId)
        super<FirstUseLocal>.setUp()

        signOut()
    }

    @Test
    fun happyWorkflowMainFeatures() {
        log("bucket01.HappyWorkflowAllMainFeatures.happyWorkflowMainFeatures")

        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(
            *MockFinger.person1TwoFingersGoodScan,
            *MockFinger.person1TwoFingersAgainGoodScan,
            *MockFinger.person1TwoFingersAgainGoodScan)))

        // Launch and sign in
        launchActivityEnrol(calloutCredentials, enrolTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()
        // Once signed in proceed to enrol workflow
        fullHappyWorkflow()
        collectFingerprintsEnrolmentCheckFinished(enrolTestRule)
        val guid = enrolmentReturnedResult(enrolTestRule)

        // Launch app and do an identification workflow
        launchActivityIdentify(calloutCredentials, identifyTestRule)
        fullHappyWorkflow()
        matchingActivityIdentificationCheckFinished(identifyTestRule)
        guidIsTheOnlyReturnedIdentification(identifyTestRule, guid)

        // Launch app and do a verification workflow
        launchActivityVerify(calloutCredentials, verifyTestRule, guid)
        fullHappyWorkflow()
        matchingActivityVerificationCheckFinished(verifyTestRule)
        verificationSuccessful(verifyTestRule, guid)
    }

    @Test
    fun happyWorkflowTwoPeopleMainFeatures() {
        log("bucket01.HappyWorkflowAllMainFeatures.happyWorkflowTwoPeopleMainFeatures")

        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(
            *MockFinger.person1TwoFingersGoodScan,
            *MockFinger.person2TwoFingersGoodScan,
            *MockFinger.person1TwoFingersAgainGoodScan,
            *MockFinger.person2TwoFingersAgainGoodScan,
            *MockFinger.person1TwoFingersAgainGoodScan,
            *MockFinger.person2TwoFingersAgainGoodScan,
            *MockFinger.person1TwoFingersAgainGoodScan,
            *MockFinger.person2TwoFingersAgainGoodScan)))

        // Launch and sign in
        launchActivityEnrol(calloutCredentials, enrolTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()
        // Once signed in proceed to enrol person1
        fullHappyWorkflow()
        collectFingerprintsEnrolmentCheckFinished(enrolTestRule)
        val person1 = enrolmentReturnedResult(enrolTestRule)

        // Launch app and enrol person2
        launchActivityEnrol(calloutCredentials, enrolTestRule)
        fullHappyWorkflow()
        collectFingerprintsEnrolmentCheckFinished(enrolTestRule)
        val person2 = enrolmentReturnedResult(enrolTestRule)

        // Launch app and do an identification with person 1
        launchActivityIdentify(calloutCredentials, identifyTestRule)
        fullHappyWorkflow()
        matchingActivityIdentificationCheckFinished(identifyTestRule)
        twoReturnedIdentificationsOneMatchOneNotMatch(identifyTestRule, person1, person2)

        // Launch app and do an identification with person 2
        launchActivityIdentify(calloutCredentials, identifyTestRule)
        fullHappyWorkflow()
        matchingActivityIdentificationCheckFinished(identifyTestRule)
        twoReturnedIdentificationsOneMatchOneNotMatch(identifyTestRule, person2, person1)

        // Launch app and do a verification with person 1, should match
        launchActivityVerify(calloutCredentials, verifyTestRule, person1)
        fullHappyWorkflow()
        matchingActivityVerificationCheckFinished(verifyTestRule)
        verificationSuccessful(verifyTestRule, person1)

        // Launch app and do a verification with person 2, should match
        launchActivityVerify(calloutCredentials, verifyTestRule, person2)
        fullHappyWorkflow()
        matchingActivityVerificationCheckFinished(verifyTestRule)
        verificationSuccessful(verifyTestRule, person2)

        // Launch app and do a verification with person 1 pretending to be person 2, should not match
        launchActivityVerify(calloutCredentials, verifyTestRule, person2)
        fullHappyWorkflow()
        matchingActivityVerificationCheckFinished(verifyTestRule)
        verificationNotAMatch(verifyTestRule, person2)

        // Launch app and do a verification with person 2 pretending to be person 1, should not match
        launchActivityVerify(calloutCredentials, verifyTestRule, person1)
        fullHappyWorkflow()
        matchingActivityVerificationCheckFinished(verifyTestRule)
        verificationNotAMatch(verifyTestRule, person1)
    }

    private fun signOut() {
        remoteDbManager.signOutOfRemoteDb()
    }
}
