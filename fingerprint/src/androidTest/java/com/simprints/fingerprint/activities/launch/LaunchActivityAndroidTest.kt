package com.simprints.fingerprint.activities.launch

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.simprints.fingerprint.R
import com.simprints.fingerprint.commontesttools.di.TestFingerprintCoreModule
import com.simprints.fingerprint.commontesttools.di.TestFingerprintModule
import com.simprints.fingerprint.controllers.consentdata.ConsentDataManager
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.simnetworkutils.FingerprintSimNetworkUtils
import com.simprints.fingerprint.controllers.scanner.ScannerManager
import com.simprints.fingerprint.data.domain.consent.GeneralConsent
import com.simprints.fingerprint.data.domain.consent.ParentalConsent
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.FingerprintToDomainRequest.fromFingerprintToDomainRequest
import com.simprints.fingerprint.exceptions.safe.setup.BluetoothNotEnabledException
import com.simprints.fingerprint.exceptions.safe.setup.MultipleScannersPairedException
import com.simprints.fingerprint.exceptions.safe.setup.ScannerLowBatteryException
import com.simprints.fingerprint.exceptions.safe.setup.ScannerNotPairedException
import com.simprints.fingerprint.exceptions.unexpected.UnknownBluetoothIssueException
import com.simprints.fingerprint.testtools.AndroidTestConfig
import com.simprints.fingerprintscanner.Scanner
import com.simprints.fingerprintscannermock.MockBluetoothAdapter
import com.simprints.fingerprintscannermock.MockScannerManager
import com.simprints.id.domain.alert.AlertActivityViewModel
import com.simprints.moduleapi.fingerprint.requests.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.requests.IMatchGroup
import com.simprints.testtools.android.waitOnUi
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.whenever
import io.reactivex.Completable
import io.reactivex.Single
import junit.framework.Assert.assertEquals
import junit.framework.TestCase
import kotlinx.android.synthetic.main.activity_launch.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class LaunchActivityAndroidTest {

    @get:Rule var permissionRule: GrantPermissionRule? = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)
    @get:Rule val launchActivityRule = ActivityTestRule(LaunchActivity::class.java, false, false)

    private var mockBluetoothAdapter: MockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager())
    @Inject lateinit var scannerManagerSpy: ScannerManager
    @Inject lateinit var dbManagerMock: FingerprintDbManager
    @Inject lateinit var simNetworkUtilsMock: FingerprintSimNetworkUtils
    @Inject lateinit var consentDataManagerMock: ConsentDataManager

    private val fingerprintModule by lazy {
        TestFingerprintModule(
            scannerManagerRule = DependencyRule.SpyRule,
            consentDataManagerRule = DependencyRule.MockRule,
            bluetoothComponentAdapter = DependencyRule.ReplaceRule { mockBluetoothAdapter })
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
    }

    private fun mockDefaultDbManager() {
        whenever(dbManagerMock) { loadPerson(anyNotNull(), anyNotNull()) } thenReturn Single.error(IllegalStateException())
    }

    @Test
    fun notScannerFromInitVeroStep_shouldAnErrorAlert() {
        whenever(scannerManagerSpy) { initVero() } thenReturn Completable.error(ScannerNotPairedException())
        launchActivityRule.launchActivity(enrolRequest.toIntent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(AlertActivityViewModel.NOT_PAIRED.title)))
    }

    @Test
    fun multiScannersPairedFromInitVeroStep_shouldAnErrorAlert() {
        whenever(scannerManagerSpy) { initVero() } thenReturn Completable.error(MultipleScannersPairedException())
        launchActivityRule.launchActivity(enrolRequest.toIntent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(AlertActivityViewModel.MULTIPLE_PAIRED_SCANNERS.title)))
    }

    @Test
    fun bluetoothOffFromConnectVeroStep_shouldAnErrorAlert() {
        makeInitVeroStepSucceeding()

        whenever(scannerManagerSpy) { connectToVero() } thenReturn Completable.error(BluetoothNotEnabledException())
        launchActivityRule.launchActivity(enrolRequest.toIntent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(AlertActivityViewModel.BLUETOOTH_NOT_ENABLED.title)))
    }

    @Test
    fun bluetoothNotSupportedFromConnectVeroStep_shouldAnErrorAlert() {
        makeInitVeroStepSucceeding()

        whenever(scannerManagerSpy) { connectToVero() } thenReturn Completable.error(BluetoothNotEnabledException())
        launchActivityRule.launchActivity(enrolRequest.toIntent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(AlertActivityViewModel.BLUETOOTH_NOT_SUPPORTED.title)))
    }

    @Test
    fun bluetoothNotPairedFromConnectVeroStep_shouldAnErrorAlert() {
        makeInitVeroStepSucceeding()

        whenever(scannerManagerSpy) { connectToVero() } thenReturn Completable.error(ScannerNotPairedException())
        launchActivityRule.launchActivity(enrolRequest.toIntent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(AlertActivityViewModel.NOT_PAIRED.title)))
    }

    @Test
    fun unknownBluetoothIssueFromConnectVeroStep_shouldAnErrorAlert() {
        makeInitVeroStepSucceeding()

        whenever(scannerManagerSpy) { connectToVero() } thenReturn Completable.error(UnknownBluetoothIssueException())
        launchActivityRule.launchActivity(enrolRequest.toIntent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(AlertActivityViewModel.DISCONNECTED.title)))
    }

    @Test
    fun unknownBluetoothIssueFromResetUIVeroStep_shouldAnErrorAlert() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepSucceeding()

        whenever(scannerManagerSpy) { resetVeroUI() } thenReturn Completable.error(UnknownBluetoothIssueException())
        launchActivityRule.launchActivity(enrolRequest.toIntent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(AlertActivityViewModel.DISCONNECTED.title)))
    }

    @Test
    fun lowBatteryFromWakingUpVeroStep_shouldAnErrorAlert() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepSucceeding()
        makeResetVeroUISucceeding()

        whenever(scannerManagerSpy) { wakeUpVero() } thenReturn Completable.error(ScannerLowBatteryException())
        launchActivityRule.launchActivity(enrolRequest.toIntent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(AlertActivityViewModel.LOW_BATTERY.title)))
    }

    @Test
    fun unknownBluetoothIssueFromWakingUpVeroStep_shouldAnErrorAlert() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepSucceeding()
        makeResetVeroUISucceeding()

        whenever(scannerManagerSpy) { wakeUpVero() } thenReturn Completable.error(UnknownBluetoothIssueException())
        launchActivityRule.launchActivity(enrolRequest.toIntent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(AlertActivityViewModel.DISCONNECTED.title)))
    }

    @Test
    fun guidForVerificationNotAvailableLocallyAndPhoneIsOffline_shouldAnErrorAlert() {
        makeSetupVeroSucceeding()

        whenever(dbManagerMock) { loadPerson(anyNotNull(), anyNotNull()) } thenReturn Single.error(IllegalStateException())
        whenever(simNetworkUtilsMock) { isConnected() } thenReturn false

        launchActivityRule.launchActivity(verifyRequest.toIntent())

        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(AlertActivityViewModel.GUID_NOT_FOUND_OFFLINE.title)))
    }

    @Test
    fun guidForVerificationNotAvailableLocallyAndPhoneIsOnline_shouldAnErrorAlert() {
        makeSetupVeroSucceeding()

        whenever(dbManagerMock) { loadPerson(anyNotNull(), anyNotNull()) } thenReturn Single.error(IllegalStateException())
        whenever(simNetworkUtilsMock) { isConnected() } thenReturn true

        Timber.e("DbManager in test version is : $dbManagerMock")
        Timber.e("ScannerManager in test version is : $scannerManagerSpy")

        launchActivityRule.launchActivity(verifyRequest.toIntent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(AlertActivityViewModel.GUID_NOT_FOUND_ONLINE.title)))
    }

    @Test
    fun enrollmentCallout_showsCorrectGeneralConsentTextAndNoParentalByDefault() {
        mockDefaultConsentDataManager(parentalConsentExists = false)

        launchActivityRule.launchActivity(enrolRequest.toIntent())
        val activity = launchActivityRule.activity

        val generalConsentText = activity.generalConsentTextView.text.toString()
        val defaultGeneralConsentText = GeneralConsent().assembleText(
            activity,
            fromFingerprintToDomainRequest(enrolRequest),
            DEFAULT_PROGRAM_NAME,
            DEFAULT_ORGANISATION_NAME)

        assertEquals(defaultGeneralConsentText, generalConsentText)

        val parentConsentText = activity.parentalConsentTextView.text.toString()
        TestCase.assertEquals("", parentConsentText)
    }

    @Test
    fun identifyCallout_showsCorrectGeneralConsentTextAndNoParentalByDefault() {
        mockDefaultConsentDataManager(parentalConsentExists = false)

        launchActivityRule.launchActivity(identifyRequest.toIntent())
        val activity = launchActivityRule.activity

        val generalConsentText = activity.generalConsentTextView.text.toString()
        val defaultGeneralConsentText = GeneralConsent().assembleText(activity,
            fromFingerprintToDomainRequest(identifyRequest),
            DEFAULT_PROGRAM_NAME,
            DEFAULT_ORGANISATION_NAME)
        assertEquals(defaultGeneralConsentText, generalConsentText)

        val parentConsentText = activity.parentalConsentTextView.text.toString()
        TestCase.assertEquals("", parentConsentText)
    }

    @Test
    fun enrollmentCallout_showsBothConsentsCorrectlyWhenParentalConsentExists() {
        mockDefaultConsentDataManager(parentalConsentExists = true)

        launchActivityRule.launchActivity(enrolRequest.toIntent())
        val activity = launchActivityRule.activity

        val generalConsentText = activity.generalConsentTextView.text.toString()
        val defaultGeneralConsentText = GeneralConsent().assembleText(activity,
            fromFingerprintToDomainRequest(enrolRequest),
            DEFAULT_PROGRAM_NAME,
            DEFAULT_ORGANISATION_NAME)
        assertEquals(defaultGeneralConsentText, generalConsentText)

        val parentConsentText = activity.parentalConsentTextView.text.toString()
        val defaultParentalConsentText = ParentalConsent().assembleText(activity,
            fromFingerprintToDomainRequest(enrolRequest),
            DEFAULT_PROGRAM_NAME,
            DEFAULT_ORGANISATION_NAME)
        assertEquals(defaultParentalConsentText, parentConsentText)
    }

    @Test
    fun identifyCallout_showsBothConsentsCorrectlyWhenParentalConsentExists() {
        mockDefaultConsentDataManager(parentalConsentExists = true)

        launchActivityRule.launchActivity(identifyRequest.toIntent())
        val activity = launchActivityRule.activity

        val generalConsentText = activity.generalConsentTextView.text.toString()
        val defaultGeneralConsentText = GeneralConsent().assembleText(activity,
            fromFingerprintToDomainRequest(identifyRequest),
            DEFAULT_PROGRAM_NAME,
            DEFAULT_ORGANISATION_NAME)
        assertEquals(defaultGeneralConsentText, generalConsentText)

        val parentConsentText = activity.parentalConsentTextView.text.toString()
        val defaultParentalConsentText = ParentalConsent().assembleText(activity,
            fromFingerprintToDomainRequest(identifyRequest),
            DEFAULT_PROGRAM_NAME,
            DEFAULT_ORGANISATION_NAME)
        assertEquals(defaultParentalConsentText, parentConsentText)
    }

    @Test
    fun malformedConsentJson_showsDefaultConsent() {
        mockDefaultConsentDataManager(generalConsentOptions = MALFORMED_CONSENT_OPTIONS)

        launchActivityRule.launchActivity(enrolRequest.toIntent())
        val activity = launchActivityRule.activity

        val generalConsentText = activity.generalConsentTextView.text.toString()
        val defaultGeneralConsentText = GeneralConsent().assembleText(activity,
            fromFingerprintToDomainRequest(enrolRequest),
            DEFAULT_PROGRAM_NAME,
            DEFAULT_ORGANISATION_NAME)
        assertEquals(defaultGeneralConsentText, generalConsentText)
    }

    @Test
    fun extraUnrecognisedConsentOptions_stillShowsCorrectValues() {
        mockDefaultConsentDataManager(generalConsentOptions = EXTRA_UNRECOGNISED_CONSENT_OPTIONS)

        launchActivityRule.launchActivity(enrolRequest.toIntent())
        val activity = launchActivityRule.activity

        val generalConsentText = activity.generalConsentTextView.text.toString()
        val targetConsentText = EXTRA_UNRECOGNISED_CONSENT_TARGET.assembleText(activity,
            fromFingerprintToDomainRequest(enrolRequest),
            DEFAULT_PROGRAM_NAME,
            DEFAULT_ORGANISATION_NAME)
        assertEquals(targetConsentText, generalConsentText)
    }

    @Test
    fun partiallyMissingConsentOptions_stillShowsCorrectValues() {
        mockDefaultConsentDataManager(generalConsentOptions = PARTIALLY_MISSING_CONSENT_OPTIONS)

        launchActivityRule.launchActivity(enrolRequest.toIntent())
        val activity = launchActivityRule.activity

        val generalConsentText = activity.generalConsentTextView.text.toString()
        val targetConsentText = PARTIALLY_MISSING_CONSENT_TARGET.assembleText(
            activity, 
            fromFingerprintToDomainRequest(enrolRequest),
            DEFAULT_PROGRAM_NAME,
            DEFAULT_ORGANISATION_NAME)
        assertEquals(targetConsentText, generalConsentText)
    }


    private fun mockDefaultConsentDataManager(parentalConsentExists: Boolean = false,
                                              generalConsentOptions: String = REMOTE_CONSENT_GENERAL_OPTIONS,
                                              parentalConsentOptions: String = REMOTE_CONSENT_PARENTAL_OPTIONS) {
        whenever(consentDataManagerMock) { this.parentalConsentExists } thenReturn parentalConsentExists
        whenever(consentDataManagerMock) { this.generalConsentOptionsJson } thenReturn generalConsentOptions
        whenever(consentDataManagerMock) { this.parentalConsentOptionsJson } thenReturn parentalConsentOptions

    }


    private fun makeSetupVeroSucceeding() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepSucceeding()
        makeResetVeroUISucceeding()
        makeWakingUpVeroStepSucceeding()
    }

    private fun makeResetVeroUISucceeding() {
        whenever(scannerManagerSpy) { resetVeroUI() } thenReturn Completable.complete()
    }

    private fun makeConnectToVeroStepSucceeding() {
        whenever(scannerManagerSpy) { connectToVero() } thenReturn Completable.complete()
    }

    private fun makeWakingUpVeroStepSucceeding() {
        whenever(scannerManagerSpy) { wakeUpVero() } thenReturn Completable.complete()
    }

    private fun makeInitVeroStepSucceeding() {
        whenever(scannerManagerSpy) { initVero() } thenReturn Completable.complete()
        scannerManagerSpy.scanner = Scanner(MAC_ADDRESS, mockBluetoothAdapter)
    }

    companion object {
        private const val DEFAULT_PROJECT_ID = "some_project_id"
        private const val DEFAULT_USER_ID = "some_user_id"
        private const val DEFAULT_MODULE_ID = "some_module_id"
        private const val DEFAULT_METADATA = ""
        private const val DEFAULT_LANGUAGE = "en"
        private val DEFAULT_FINGER_STATUS = mapOf(
            IFingerIdentifier.RIGHT_THUMB to false,
            IFingerIdentifier.RIGHT_INDEX_FINGER to false,
            IFingerIdentifier.RIGHT_3RD_FINGER to false,
            IFingerIdentifier.RIGHT_4TH_FINGER to false,
            IFingerIdentifier.RIGHT_5TH_FINGER to false,
            IFingerIdentifier.LEFT_THUMB to true,
            IFingerIdentifier.LEFT_INDEX_FINGER to true,
            IFingerIdentifier.LEFT_3RD_FINGER to false,
            IFingerIdentifier.LEFT_4TH_FINGER to false,
            IFingerIdentifier.LEFT_5TH_FINGER to false
        )
        private const val DEFAULT_LOGO_EXISTS = true
        private const val DEFAULT_PROGRAM_NAME = "This program"
        private const val DEFAULT_ORGANISATION_NAME = "This organisation"
        private const val DEFAULT_VERIFY_GUID = "verify_guid"

        private const val MAC_ADDRESS = "F0:AC:D7:C8:CB:22"

        private val enrolRequest = FingerprintEnrolRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID,
            DEFAULT_MODULE_ID, DEFAULT_METADATA, DEFAULT_LANGUAGE, DEFAULT_FINGER_STATUS,
            DEFAULT_LOGO_EXISTS, DEFAULT_PROGRAM_NAME, DEFAULT_ORGANISATION_NAME)

        private val verifyRequest = FingerprintVerifyRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID,
            DEFAULT_MODULE_ID, DEFAULT_METADATA, DEFAULT_LANGUAGE, DEFAULT_FINGER_STATUS,
            DEFAULT_LOGO_EXISTS, DEFAULT_PROGRAM_NAME, DEFAULT_ORGANISATION_NAME, DEFAULT_VERIFY_GUID)

        private val identifyRequest = FingerprintIdentifyRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID,
            DEFAULT_MODULE_ID, DEFAULT_METADATA, DEFAULT_LANGUAGE, DEFAULT_FINGER_STATUS,
            DEFAULT_LOGO_EXISTS, DEFAULT_PROGRAM_NAME, DEFAULT_ORGANISATION_NAME, IMatchGroup.GLOBAL, 1)

        private const val REMOTE_CONSENT_GENERAL_OPTIONS = "{\"consent_enrol_only\":false,\"consent_enrol\":true,\"consent_id_verify\":true,\"consent_share_data_no\":true,\"consent_share_data_yes\":false,\"consent_collect_yes\":false,\"consent_privacy_rights\":true,\"consent_confirmation\":true}"
        private const val REMOTE_CONSENT_PARENTAL_OPTIONS = "{\"consent_parent_enrol_only\":false,\"consent_parent_enrol\":true,\"consent_parent_id_verify\":true,\"consent_parent_share_data_no\":true,\"consent_parent_share_data_yes\":false,\"consent_collect_yes\":false,\"consent_parent_privacy_rights\":true,\"consent_parent_confirmation\":true}"

        private const val MALFORMED_CONSENT_OPTIONS = "gibberish{\"000}\"\""

        private const val EXTRA_UNRECOGNISED_CONSENT_OPTIONS = "{\"consent_enrol_only\":true,\"consent_enrol\":false,\"consent_id_verify\":true,\"consent_share_data_no\":true,\"consent_share_data_yes\":false,\"consent_collect_yes\":false,\"consent_privacy_rights\":true,\"consent_confirmation\":true,\"this_one_doesnt_exist\":true}"
        private val EXTRA_UNRECOGNISED_CONSENT_TARGET = GeneralConsent(consentEnrolOnly = true, consentEnrol = false)

        private const val PARTIALLY_MISSING_CONSENT_OPTIONS = "{\"consent_enrol_only\":true,\"consent_enrol\":false,\"consent_id_verify\":true,\"consent_share_data_no\":true,\"consent_share_data_yes\":false,\"consent_collect_yes\":false}"
        private val PARTIALLY_MISSING_CONSENT_TARGET = GeneralConsent(consentEnrolOnly = true, consentEnrol = false)
    }
}
