package com.simprints.id.coreFeatures

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.simprints.id.Application
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_REALM_KEY
import com.simprints.id.commontesttools.di.DependencyRule.MockRule
import com.simprints.id.commontesttools.di.DependencyRule.ReplaceRule
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.testSnippets.*
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.id.testtools.TestProjectRule
import com.simprints.id.testtools.checkLoginFromIntentActivityTestRule
import com.simprints.id.testtools.integration.adapters.toCalloutCredentials
import com.simprints.id.testtools.integration.models.TestProject
import com.simprints.id.tools.RandomGenerator
import com.simprints.mockscanner.MockBluetoothAdapter
import com.simprints.mockscanner.MockFinger
import com.simprints.mockscanner.MockScannerManager
import com.simprints.testframework.android.log
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
class HappyWorkflowAllMainFeatures {

    private val app = ApplicationProvider.getApplicationContext<Application>()

    @get:Rule val testProjectRule = TestProjectRule()
    private lateinit var testProject: TestProject

    @get:Rule val enrolTestRule1 = checkLoginFromIntentActivityTestRule()
    @get:Rule val enrolTestRule2 = checkLoginFromIntentActivityTestRule()
    @get:Rule val identifyTestRule1 = checkLoginFromIntentActivityTestRule()
    @get:Rule val identifyTestRule2 = checkLoginFromIntentActivityTestRule()
    @get:Rule val verifyTestRule1 = checkLoginFromIntentActivityTestRule()
    @get:Rule val verifyTestRule2 = checkLoginFromIntentActivityTestRule()
    @get:Rule val verifyTestRule3 = checkLoginFromIntentActivityTestRule()
    @get:Rule val verifyTestRule4 = checkLoginFromIntentActivityTestRule()

    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var randomGeneratorMock: RandomGenerator
    private lateinit var mockBluetoothAdapter: MockBluetoothAdapter

    private val module by lazy {
        TestAppModule(app,
            randomGeneratorRule = MockRule,
            bluetoothComponentAdapterRule = ReplaceRule { mockBluetoothAdapter })
    }

    @Before
    fun setUp() {
        log("HappyWorkflowAllMainFeatures.setUp()")
        AndroidTestConfig(this, module).fullSetup()

        setupRandomGeneratorToGenerateKey(DEFAULT_REALM_KEY, randomGeneratorMock)

        testProject = testProjectRule.testProject

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

    private fun signOut() {
        remoteDbManager.signOutOfRemoteDb()
    }
}
