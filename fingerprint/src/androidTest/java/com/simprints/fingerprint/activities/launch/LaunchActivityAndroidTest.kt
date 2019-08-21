package com.simprints.fingerprint.activities.launch

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.AlertActivityViewModel
import com.simprints.fingerprint.activities.launch.request.LaunchTaskRequest
import com.simprints.fingerprint.commontesttools.di.TestFingerprintCoreModule
import com.simprints.fingerprint.commontesttools.di.TestFingerprintModule
import com.simprints.fingerprint.commontesttools.scanner.setupScannerManagerMockWithMockedScanner
import com.simprints.fingerprint.controllers.consentdata.ConsentDataManager
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.simnetworkutils.FingerprintSimNetworkUtils
import com.simprints.fingerprint.controllers.scanner.ScannerManager
import com.simprints.fingerprint.data.domain.Action
import com.simprints.fingerprint.data.domain.consent.GeneralConsent
import com.simprints.fingerprint.data.domain.consent.ParentalConsent
import com.simprints.fingerprint.exceptions.safe.scanner.BluetoothNotEnabledException
import com.simprints.fingerprint.exceptions.safe.scanner.MultipleScannersPairedException
import com.simprints.fingerprint.exceptions.safe.scanner.ScannerLowBatteryException
import com.simprints.fingerprint.exceptions.safe.scanner.ScannerNotPairedException
import com.simprints.fingerprint.exceptions.unexpected.scanner.UnknownScannerIssueException
import com.simprints.fingerprint.testtools.AndroidTestConfig
import com.simprints.fingerprintscannermock.dummy.DummyBluetoothAdapter
import com.simprints.id.Application
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.whenThis
import com.simprints.testtools.common.syntax.whenever
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.android.synthetic.main.activity_launch.*
import org.hamcrest.CoreMatchers.containsString
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class LaunchActivityAndroidTest {

    @get:Rule var permissionRule: GrantPermissionRule? = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    private lateinit var scenario: ActivityScenario<LaunchActivity>

    @Inject lateinit var scannerManagerSpy: ScannerManager
    @Inject lateinit var dbManagerMock: FingerprintDbManager
    @Inject lateinit var simNetworkUtilsMock: FingerprintSimNetworkUtils
    @Inject lateinit var consentDataManagerMock: ConsentDataManager

    private val fingerprintModule by lazy {
        TestFingerprintModule(
            scannerManagerRule = DependencyRule.SpyRule,
            consentDataManagerRule = DependencyRule.MockRule,
            bluetoothComponentAdapter = DependencyRule.ReplaceRule { DummyBluetoothAdapter() })
    }

    private val fingerprintCoreModule by lazy {
        TestFingerprintCoreModule(
            fingerprintDbManagerRule = DependencyRule.MockRule,
            fingerprintSimNetworkUtilsRule = DependencyRule.MockRule)
    }

    @Before
    fun setUp() {
        AndroidTestConfig(this, fingerprintModule, fingerprintCoreModule).fullSetup()
        mockDefaultConsentDataManager(true)
        mockDefaultDbManager()
        scannerManagerSpy.setupScannerManagerMockWithMockedScanner()
    }

    private fun mockDefaultDbManager() {
        whenever(dbManagerMock) { loadPerson(anyNotNull(), anyNotNull()) } thenReturn Single.error(IllegalStateException())
    }

    @Test
    fun notScannerFromInitVeroStep_shouldShowAnErrorAlert() {
        makeInitVeroStepFailing(ScannerNotPairedException())

        scenario = ActivityScenario.launch(launchTaskRequest(Action.ENROL).toIntent())

        onView(withId(R.id.alertTitle)).check(matches(withText(AlertActivityViewModel.NOT_PAIRED.title)))
    }

    @Test
    fun multiScannersPairedFromInitVeroStep_shouldShowAnErrorAlert() {
        makeInitVeroStepFailing(MultipleScannersPairedException())

        scenario = ActivityScenario.launch(launchTaskRequest(Action.ENROL).toIntent())

        onView(withId(R.id.alertTitle)).check(matches(withText(AlertActivityViewModel.MULTIPLE_PAIRED_SCANNERS.title)))
    }

    @Test
    fun bluetoothOffFromConnectVeroStep_shouldShowAnErrorAlert() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepFailing(BluetoothNotEnabledException())

        scenario = ActivityScenario.launch(launchTaskRequest(Action.ENROL).toIntent())

        onView(withId(R.id.alertTitle)).check(matches(withText(AlertActivityViewModel.BLUETOOTH_NOT_ENABLED.title)))
    }

    @Test
    fun bluetoothNotSupportedFromConnectVeroStep_shouldShowAnErrorAlert() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepFailing(BluetoothNotEnabledException())

        scenario = ActivityScenario.launch(launchTaskRequest(Action.ENROL).toIntent())

        onView(withId(R.id.alertTitle)).check(matches(withText(AlertActivityViewModel.BLUETOOTH_NOT_SUPPORTED.title)))
    }

    @Test
    fun bluetoothNotPairedFromConnectVeroStep_shouldShowAnErrorAlert() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepFailing(ScannerNotPairedException())

        scenario = ActivityScenario.launch(launchTaskRequest(Action.ENROL).toIntent())

        onView(withId(R.id.alertTitle)).check(matches(withText(AlertActivityViewModel.NOT_PAIRED.title)))
    }

    @Test
    fun unknownBluetoothIssueFromConnectVeroStep_shouldShowScannerErrorConfirmDialog() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepFailing(UnknownScannerIssueException())

        scenario = ActivityScenario.launch(launchTaskRequest(Action.ENROL).toIntent())

        onView(withText(containsString("your scanner?")))
            .inRoot(RootMatchers.isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun unknownBluetoothIssueFromConnectVeroSetup_clickYes_shouldShowCorrectAlert() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepFailing(UnknownScannerIssueException())

        scenario = ActivityScenario.launch(launchTaskRequest(Action.ENROL).toIntent())

        onView(withText(containsString("your scanner?")))
            .inRoot(RootMatchers.isDialog()).check(matches(isDisplayed()))
        onView(withText(R.string.scanner_confirmation_yes)).perform(click())

        onView(withId(R.id.alertTitle)).check(matches(withText(AlertActivityViewModel.DISCONNECTED.title)))
    }

    @Test
    fun unknownBluetoothIssueFromConnectVeroSetup_clickNo_shouldShowCorrectAlert() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepFailing(UnknownScannerIssueException())

        scenario = ActivityScenario.launch(launchTaskRequest(Action.ENROL).toIntent())

        onView(withText(containsString("your scanner?")))
            .inRoot(RootMatchers.isDialog()).check(matches(isDisplayed()))
        onView(withText(R.string.scanner_confirmation_no)).perform(click())

        onView(withId(R.id.alertTitle)).check(matches(withText(AlertActivityViewModel.NOT_PAIRED.title)))
    }

    @Test
    fun unknownBluetoothIssueFromResetUIVeroStep_shouldShowErrorConfirmDialog() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepSucceeding()
        makeResetVeroUIStepFailing(UnknownScannerIssueException())

        scenario = ActivityScenario.launch(launchTaskRequest(Action.ENROL).toIntent())

        onView(withText(containsString("your scanner?")))
            .inRoot(RootMatchers.isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun lowBatteryFromWakingUpVeroStep_shouldShowAnErrorAlert() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepSucceeding()
        makeResetVeroUIStepSucceeding()
        makeWakeUpVeroStepFailing(ScannerLowBatteryException())

        scenario = ActivityScenario.launch(launchTaskRequest(Action.ENROL).toIntent())

        onView(withId(R.id.alertTitle)).check(matches(withText(AlertActivityViewModel.LOW_BATTERY.title)))
    }

    @Test
    fun unknownBluetoothIssueFromWakingUpVeroStep_shouldShowErrorConfirmDialog() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepSucceeding()
        makeResetVeroUIStepSucceeding()
        makeWakeUpVeroStepFailing(UnknownScannerIssueException())

        scenario = ActivityScenario.launch(launchTaskRequest(Action.ENROL).toIntent())

        onView(withText(containsString("your scanner?")))
            .inRoot(RootMatchers.isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun guidForVerificationNotAvailableLocallyAndPhoneIsOffline_shouldShowAnErrorAlert() {
        makeSetupVeroSucceeding()

        whenever(dbManagerMock) { loadPerson(anyNotNull(), anyNotNull()) } thenReturn Single.error(IllegalStateException())
        whenever(simNetworkUtilsMock) { isConnected() } thenReturn false

        scenario = ActivityScenario.launch(launchTaskRequest(Action.VERIFY).toIntent())

        onView(withId(R.id.alertTitle)).check(matches(withText(AlertActivityViewModel.GUID_NOT_FOUND_OFFLINE.title)))
    }

    @Test
    fun guidForVerificationNotAvailableLocallyAndPhoneIsOnline_shouldShowAnErrorAlert() {
        makeSetupVeroSucceeding()

        whenever(dbManagerMock) { loadPerson(anyNotNull(), anyNotNull()) } thenReturn Single.error(IllegalStateException())
        whenever(simNetworkUtilsMock) { isConnected() } thenReturn true

        Timber.e("DbManager in test version is : $dbManagerMock")
        Timber.e("ScannerManager in test version is : $scannerManagerSpy")

        scenario = ActivityScenario.launch(launchTaskRequest(Action.VERIFY).toIntent())

        onView(withId(R.id.alertTitle)).check(matches(withText(AlertActivityViewModel.GUID_NOT_FOUND_ONLINE.title)))
    }

    @Test
    fun enrollmentCallout_showsCorrectGeneralConsentTextAndNoParentalByDefault() {
        mockDefaultConsentDataManager(hasParentalConsent = false)

        scenario = ActivityScenario.launch(launchTaskRequest(Action.ENROL).toIntent())
        scenario.onActivity { activity ->
            val generalConsentText = activity.generalConsentTextView.text.toString()
            val defaultGeneralConsentText = GeneralConsent().assembleText(
                activity,
                launchTaskRequest(Action.ENROL),
                DEFAULT_PROGRAM_NAME,
                DEFAULT_ORGANISATION_NAME)

            assertEquals(defaultGeneralConsentText, generalConsentText)

            val parentConsentText = activity.parentalConsentTextView.text.toString()
            assertEquals("", parentConsentText)
        }
    }

    @Test
    fun identifyCallout_showsCorrectGeneralConsentTextAndNoParentalByDefault() {
        mockDefaultConsentDataManager(hasParentalConsent = false)

        scenario = ActivityScenario.launch(launchTaskRequest(Action.IDENTIFY).toIntent())
        scenario.onActivity { activity ->
            val generalConsentText = activity.generalConsentTextView.text.toString()
            val defaultGeneralConsentText = GeneralConsent().assembleText(activity,
                launchTaskRequest(Action.IDENTIFY),
                DEFAULT_PROGRAM_NAME,
                DEFAULT_ORGANISATION_NAME)
            assertEquals(defaultGeneralConsentText, generalConsentText)

            val parentConsentText = activity.parentalConsentTextView.text.toString()
            assertEquals("", parentConsentText)
        }
    }

    @Test
    fun enrollmentCallout_showsBothConsentsCorrectlyWhenParentalConsentExists() {
        mockDefaultConsentDataManager(hasParentalConsent = true)

        scenario = ActivityScenario.launch(launchTaskRequest(Action.ENROL).toIntent())
        scenario.onActivity { activity ->
            val generalConsentText = activity.generalConsentTextView.text.toString()
            val defaultGeneralConsentText = GeneralConsent().assembleText(activity,
                launchTaskRequest(Action.ENROL),
                DEFAULT_PROGRAM_NAME,
                DEFAULT_ORGANISATION_NAME)
            assertEquals(defaultGeneralConsentText, generalConsentText)

            val parentConsentText = activity.parentalConsentTextView.text.toString()
            val defaultParentalConsentText = ParentalConsent().assembleText(activity,
                launchTaskRequest(Action.ENROL),
                DEFAULT_PROGRAM_NAME,
                DEFAULT_ORGANISATION_NAME)
            assertEquals(defaultParentalConsentText, parentConsentText)
        }
    }

    @Test
    fun identifyCallout_showsBothConsentsCorrectlyWhenParentalConsentExists() {
        mockDefaultConsentDataManager(hasParentalConsent = true)

        scenario = ActivityScenario.launch(launchTaskRequest(Action.IDENTIFY).toIntent())
        scenario.onActivity { activity ->
            val generalConsentText = activity.generalConsentTextView.text.toString()
            val defaultGeneralConsentText = GeneralConsent().assembleText(activity,
                launchTaskRequest(Action.IDENTIFY),
                DEFAULT_PROGRAM_NAME,
                DEFAULT_ORGANISATION_NAME)
            assertEquals(defaultGeneralConsentText, generalConsentText)

            val parentConsentText = activity.parentalConsentTextView.text.toString()
            val defaultParentalConsentText = ParentalConsent().assembleText(activity,
                launchTaskRequest(Action.IDENTIFY),
                DEFAULT_PROGRAM_NAME,
                DEFAULT_ORGANISATION_NAME)
            assertEquals(defaultParentalConsentText, parentConsentText)
        }
    }

    @Test
    fun malformedConsentJson_showsDefaultConsent() {
        mockDefaultConsentDataManager(generalConsentOptions = MALFORMED_CONSENT_OPTIONS)

        scenario = ActivityScenario.launch(launchTaskRequest(Action.ENROL).toIntent())
        scenario.onActivity { activity ->
            val generalConsentText = activity.generalConsentTextView.text.toString()
            val defaultGeneralConsentText = GeneralConsent().assembleText(activity,
                launchTaskRequest(Action.ENROL),
                DEFAULT_PROGRAM_NAME,
                DEFAULT_ORGANISATION_NAME)
            assertEquals(defaultGeneralConsentText, generalConsentText)
        }
    }

    @Test
    fun extraUnrecognisedConsentOptions_stillShowsCorrectValues() {
        mockDefaultConsentDataManager(generalConsentOptions = EXTRA_UNRECOGNISED_CONSENT_OPTIONS)

        scenario = ActivityScenario.launch(launchTaskRequest(Action.ENROL).toIntent())
        scenario.onActivity { activity ->
            val generalConsentText = activity.generalConsentTextView.text.toString()
            val targetConsentText = EXTRA_UNRECOGNISED_CONSENT_TARGET.assembleText(activity,
                launchTaskRequest(Action.ENROL),
                DEFAULT_PROGRAM_NAME,
                DEFAULT_ORGANISATION_NAME)
            assertEquals(targetConsentText, generalConsentText)
        }
    }

    @Test
    fun partiallyMissingConsentOptions_stillShowsCorrectValues() {
        mockDefaultConsentDataManager(generalConsentOptions = PARTIALLY_MISSING_CONSENT_OPTIONS)

        scenario = ActivityScenario.launch(launchTaskRequest(Action.ENROL).toIntent())
        scenario.onActivity { activity ->
            val generalConsentText = activity.generalConsentTextView.text.toString()
            val targetConsentText = PARTIALLY_MISSING_CONSENT_TARGET.assembleText(
                activity,
                launchTaskRequest(Action.ENROL),
                DEFAULT_PROGRAM_NAME,
                DEFAULT_ORGANISATION_NAME)
            assertEquals(targetConsentText, generalConsentText)
        }
    }

    private fun mockDefaultConsentDataManager(hasParentalConsent: Boolean = false,
                                              generalConsentOptions: String = REMOTE_CONSENT_GENERAL_OPTIONS,
                                              parentalConsentOptions: String = REMOTE_CONSENT_PARENTAL_OPTIONS) {
        with(consentDataManagerMock) {
            whenThis { parentalConsentExists } thenReturn hasParentalConsent
            whenThis { generalConsentOptionsJson } thenReturn generalConsentOptions
            whenThis { parentalConsentOptionsJson } thenReturn parentalConsentOptions
        }
    }

    private fun makeSetupVeroSucceeding() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepSucceeding()
        makeResetVeroUIStepSucceeding()
        makeWakeUpVeroStepSucceeding()
    }

    private fun makeInitVeroStepSucceeding() {
        whenever(scannerManagerSpy) { initVero() } thenReturn Completable.complete()
    }

    private fun makeConnectToVeroStepSucceeding() {
        whenever(scannerManagerSpy) { connectToVero() } thenReturn Completable.complete()
    }

    private fun makeResetVeroUIStepSucceeding() {
        whenever(scannerManagerSpy) { resetVeroUI() } thenReturn Completable.complete()
    }

    private fun makeWakeUpVeroStepSucceeding() {
        whenever(scannerManagerSpy) { wakeUpVero() } thenReturn Completable.complete()
    }

    private fun makeInitVeroStepFailing(e: Exception) {
        whenever(scannerManagerSpy) { initVero() } thenReturn Completable.error(e)
    }

    private fun makeConnectToVeroStepFailing(e: Exception) {
        whenever(scannerManagerSpy) { connectToVero() } thenReturn Completable.error(e)
    }

    private fun makeResetVeroUIStepFailing(e: Exception) {
        whenever(scannerManagerSpy) { resetVeroUI() } thenReturn Completable.error(e)
    }

    private fun makeWakeUpVeroStepFailing(e: Exception) {
        whenever(scannerManagerSpy) { wakeUpVero() } thenReturn Completable.error(e)
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
    }

    companion object {
        private const val DEFAULT_PROJECT_ID = "some_project_id"
        private const val DEFAULT_LANGUAGE = "en"
        private const val DEFAULT_LOGO_EXISTS = true
        private const val DEFAULT_PROGRAM_NAME = "This program"
        private const val DEFAULT_ORGANISATION_NAME = "This organisation"
        private const val DEFAULT_VERIFY_GUID = "verify_guid"

        private fun launchTaskRequest(action: Action) = LaunchTaskRequest(
            DEFAULT_PROJECT_ID, action, DEFAULT_LANGUAGE, DEFAULT_LOGO_EXISTS, DEFAULT_PROGRAM_NAME,
            DEFAULT_ORGANISATION_NAME, if (action == Action.VERIFY) DEFAULT_VERIFY_GUID else null
        )

        private fun LaunchTaskRequest.toIntent() = Intent().also {
            it.setClassName(ApplicationProvider.getApplicationContext<Application>().packageName, LaunchActivity::class.qualifiedName!!)
            it.putExtra(LaunchTaskRequest.BUNDLE_KEY, this)
        }

        private const val REMOTE_CONSENT_GENERAL_OPTIONS = "{\"consent_enrol_only\":false,\"consent_enrol\":true,\"consent_id_verify\":true,\"consent_share_data_no\":true,\"consent_share_data_yes\":false,\"consent_collect_yes\":false,\"consent_privacy_rights\":true,\"consent_confirmation\":true}"
        private const val REMOTE_CONSENT_PARENTAL_OPTIONS = "{\"consent_parent_enrol_only\":false,\"consent_parent_enrol\":true,\"consent_parent_id_verify\":true,\"consent_parent_share_data_no\":true,\"consent_parent_share_data_yes\":false,\"consent_collect_yes\":false,\"consent_parent_privacy_rights\":true,\"consent_parent_confirmation\":true}"

        private const val MALFORMED_CONSENT_OPTIONS = "gibberish{\"000}\"\""

        private const val EXTRA_UNRECOGNISED_CONSENT_OPTIONS = "{\"consent_enrol_only\":true,\"consent_enrol\":false,\"consent_id_verify\":true,\"consent_share_data_no\":true,\"consent_share_data_yes\":false,\"consent_collect_yes\":false,\"consent_privacy_rights\":true,\"consent_confirmation\":true,\"this_one_doesnt_exist\":true}"
        private val EXTRA_UNRECOGNISED_CONSENT_TARGET = GeneralConsent(consentEnrolOnly = true, consentEnrol = false)

        private const val PARTIALLY_MISSING_CONSENT_OPTIONS = "{\"consent_enrol_only\":true,\"consent_enrol\":false,\"consent_id_verify\":true,\"consent_share_data_no\":true,\"consent_share_data_yes\":false,\"consent_collect_yes\":false}"
        private val PARTIALLY_MISSING_CONSENT_TARGET = GeneralConsent(consentEnrolOnly = true, consentEnrol = false)
    }
}
