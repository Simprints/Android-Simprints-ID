package com.simprints.id.coreFeatures

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import com.simprints.id.Application
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.di.AppModuleForAndroidTests
import com.simprints.id.di.DaggerForAndroidTests
import com.simprints.id.shared.DependencyRule.MockRule
import com.simprints.id.shared.DependencyRule.ReplaceRule
import com.simprints.id.testSnippets.*
import com.simprints.id.testTemplates.FirstUseLocalAndRemote
import com.simprints.id.testTools.ActivityUtils
import com.simprints.id.shared.DefaultTestConstants.DEFAULT_REALM_KEY
import com.simprints.id.testTools.adapters.toCalloutCredentials
import com.simprints.id.testTools.log
import com.simprints.id.testTools.models.TestProject
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.mockscanner.MockBluetoothAdapter
import com.simprints.mockscanner.MockFinger
import com.simprints.mockscanner.MockScannerManager
import io.realm.RealmConfiguration
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
class HappyWorkflowAllMainFeatures : DaggerForAndroidTests(), FirstUseLocalAndRemote {

    override var peopleRealmConfiguration: RealmConfiguration? = null
    override lateinit var testProject: TestProject

    @Rule @JvmField val enrolTestRule1 = ActivityUtils.checkLoginFromIntentActivityTestRule()
    @Rule @JvmField val enrolTestRule2 = ActivityUtils.checkLoginFromIntentActivityTestRule()
    @Rule @JvmField val identifyTestRule1 = ActivityUtils.checkLoginFromIntentActivityTestRule()
    @Rule @JvmField val identifyTestRule2 = ActivityUtils.checkLoginFromIntentActivityTestRule()
    @Rule @JvmField val verifyTestRule1 = ActivityUtils.checkLoginFromIntentActivityTestRule()
    @Rule @JvmField val verifyTestRule2 = ActivityUtils.checkLoginFromIntentActivityTestRule()
    @Rule @JvmField val verifyTestRule3 = ActivityUtils.checkLoginFromIntentActivityTestRule()
    @Rule @JvmField val verifyTestRule4 = ActivityUtils.checkLoginFromIntentActivityTestRule()

    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var randomGeneratorMock: RandomGenerator
    private lateinit var mockBluetoothAdapter: MockBluetoothAdapter

    override var module by lazyVar {
        AppModuleForAndroidTests(app,
            randomGeneratorRule = MockRule,
            bluetoothComponentAdapterRule = ReplaceRule { mockBluetoothAdapter })
    }

    @Before
    override fun setUp() {
        log("HappyWorkflowAllMainFeatures.setUp()")
        app = InstrumentationRegistry.getTargetContext().applicationContext as Application
        super<DaggerForAndroidTests>.setUp()
        testAppComponent.inject(this)

        setupRandomGeneratorToGenerateKey(DEFAULT_REALM_KEY, randomGeneratorMock)

        app.initDependencies()

        super<FirstUseLocalAndRemote>.setUp()

        signOut()
    }

    @Test
    fun happyWorkflowMainFeatures() {
        log("bucket01.HappyWorkflowAllMainFeatures.happyWorkflowMainFeatures")

        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(
            *MockFinger.person1TwoFingersGoodScan,
            *MockFinger.person1TwoFingersAgainGoodScan,
            *MockFinger.person1TwoFingersAgainGoodScan)))

        val testCredentials = testProject.toCalloutCredentials()

        // Launch and sign in
        launchActivityEnrol(testCredentials, enrolTestRule1)
        enterCredentialsDirectly(testCredentials, testProject.secret)
        pressSignIn()
        // Once signed in proceed to enrol workflow
        fullHappyWorkflow()
        collectFingerprintsEnrolmentCheckFinished(enrolTestRule1)
        val guid = enrolmentReturnedResult(enrolTestRule1)

        // Launch app and do an identification workflow
        launchActivityIdentify(testCredentials, identifyTestRule1)
        fullHappyWorkflow()
        matchingActivityIdentificationCheckFinished(identifyTestRule1)
        guidIsTheOnlyReturnedIdentification(identifyTestRule1, guid)

        // Launch app and do a verification workflow
        launchActivityVerify(testCredentials, verifyTestRule1, guid)
        fullHappyWorkflow()
        matchingActivityVerificationCheckFinished(verifyTestRule1)
        verificationSuccessful(verifyTestRule1, guid)
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

        val testCredentials = testProject.toCalloutCredentials()

        // Launch and sign in
        launchActivityEnrol(testCredentials, enrolTestRule1)
        enterCredentialsDirectly(testCredentials, testProject.secret)
        pressSignIn()
        // Once signed in proceed to enrol person1
        fullHappyWorkflow()
        collectFingerprintsEnrolmentCheckFinished(enrolTestRule1)
        val person1 = enrolmentReturnedResult(enrolTestRule1)

        // Launch app and enrol person2
        launchActivityEnrol(testCredentials, enrolTestRule2)
        fullHappyWorkflow()
        collectFingerprintsEnrolmentCheckFinished(enrolTestRule2)
        val person2 = enrolmentReturnedResult(enrolTestRule2)

        // Launch app and do an identification with person 1
        launchActivityIdentify(testCredentials, identifyTestRule1)
        fullHappyWorkflow()
        matchingActivityIdentificationCheckFinished(identifyTestRule1)
        twoReturnedIdentificationsOneMatchOneNotMatch(identifyTestRule1, person1, person2)

        // Launch app and do an identification with person 2
        launchActivityIdentify(testCredentials, identifyTestRule2)
        fullHappyWorkflow()
        matchingActivityIdentificationCheckFinished(identifyTestRule2)
        twoReturnedIdentificationsOneMatchOneNotMatch(identifyTestRule2, person2, person1)

        // Launch app and do a verification with person 1, should match
        launchActivityVerify(testCredentials, verifyTestRule1, person1)
        fullHappyWorkflow()
        matchingActivityVerificationCheckFinished(verifyTestRule1)
        verificationSuccessful(verifyTestRule1, person1)

        // Launch app and do a verification with person 2, should match
        launchActivityVerify(testCredentials, verifyTestRule2, person2)
        fullHappyWorkflow()
        matchingActivityVerificationCheckFinished(verifyTestRule2)
        verificationSuccessful(verifyTestRule2, person2)

        // Launch app and do a verification with person 1 pretending to be person 2, should not match
        launchActivityVerify(testCredentials, verifyTestRule3, person2)
        fullHappyWorkflow()
        matchingActivityVerificationCheckFinished(verifyTestRule3)
        verificationNotAMatch(verifyTestRule3, person2)

        // Launch app and do a verification with person 2 pretending to be person 1, should not match
        launchActivityVerify(testCredentials, verifyTestRule4, person1)
        fullHappyWorkflow()
        matchingActivityVerificationCheckFinished(verifyTestRule4)
        verificationNotAMatch(verifyTestRule4, person1)
    }

    @After
    override fun tearDown() {
        super.tearDown()
    }

    private fun signOut() {
        remoteDbManager.signOutOfRemoteDb()
    }
}
