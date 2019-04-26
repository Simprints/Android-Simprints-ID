package com.simprints.fingerprint.activities.launch

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.simprints.fingerprint.R
import com.simprints.fingerprint.commontesttools.DefaultTestConstants.DEFAULT_REALM_KEY
import com.simprints.fingerprint.commontesttools.di.TestAppModule
import com.simprints.fingerprint.commontesttools.di.TestFingerprintModule
import com.simprints.fingerprint.controllers.scanner.ScannerManager
import com.simprints.fingerprint.exceptions.safe.setup.BluetoothNotEnabledException
import com.simprints.fingerprint.exceptions.safe.setup.MultipleScannersPairedException
import com.simprints.fingerprint.exceptions.safe.setup.ScannerLowBatteryException
import com.simprints.fingerprint.exceptions.safe.setup.ScannerNotPairedException
import com.simprints.fingerprint.exceptions.unexpected.UnknownBluetoothIssueException
import com.simprints.fingerprint.testtools.AndroidTestConfig
import com.simprints.fingerprint.testtools.state.setupRandomGeneratorToGenerateKey
import com.simprints.fingerprintscanner.Scanner
import com.simprints.fingerprintscannermock.MockBluetoothAdapter
import com.simprints.fingerprintscannermock.MockScannerManager
import com.simprints.id.Application
import com.simprints.id.data.db.DbManager
import com.simprints.id.domain.alert.Alert
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.moduleapi.fingerprint.requests.IFingerIdentifier
import com.simprints.testtools.android.waitOnUi
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
class LaunchActivityAndroidTest {

    private val app = ApplicationProvider.getApplicationContext<Application>()

    @get:Rule var permissionRule: GrantPermissionRule? = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)
    @get:Rule val launchActivityRule = ActivityTestRule(LaunchActivity::class.java, false, false)

    private val module by lazy {
        TestAppModule(app,
            dbManagerRule = DependencyRule.MockRule,
            randomGeneratorRule = DependencyRule.MockRule,
            bluetoothComponentAdapterRule = DependencyRule.ReplaceRule { mockBluetoothAdapter },
            simNetworkUtilsRule = DependencyRule.MockRule,
            syncSchedulerHelperRule = DependencyRule.MockRule)
    }

    private val fingerprintModule by lazy {
        TestFingerprintModule(
            scannerManagerRule = DependencyRule.SpyRule
        )
    }

    private var mockBluetoothAdapter: MockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager())
    @Inject lateinit var scannerManagerSpy: ScannerManager
    @Inject lateinit var randomGeneratorMock: RandomGenerator
    @Inject lateinit var dbManagerMock: DbManager
    @Inject lateinit var simNetworkUtilsMock: SimNetworkUtils

    @Before
    fun setUp() {
        AndroidTestConfig(this, module, null, fingerprintModule).fullSetup()

        setupRandomGeneratorToGenerateKey(DEFAULT_REALM_KEY, randomGeneratorMock)
    }

    @Test
    fun notScannerFromInitVeroStep_shouldAnErrorAlert() {
        whenever(scannerManagerSpy) { initVero() } thenReturn Completable.error(ScannerNotPairedException())
        launchActivityRule.launchActivity(enrolRequest.toIntent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(Alert.NOT_PAIRED.title)))
    }

    @Test
    fun multiScannersPairedFromInitVeroStep_shouldAnErrorAlert() {
        whenever(scannerManagerSpy) { initVero() } thenReturn Completable.error(MultipleScannersPairedException())
        launchActivityRule.launchActivity(enrolRequest.toIntent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(Alert.MULTIPLE_PAIRED_SCANNERS.title)))
    }

    @Test
    fun bluetoothOffFromConnectVeroStep_shouldAnErrorAlert() {
        makeInitVeroStepSucceeding()

        whenever(scannerManagerSpy) { connectToVero() } thenReturn Completable.error(BluetoothNotEnabledException())
        launchActivityRule.launchActivity(enrolRequest.toIntent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(Alert.BLUETOOTH_NOT_ENABLED.title)))
    }

    @Test
    fun bluetoothNotSupportedFromConnectVeroStep_shouldAnErrorAlert() {
        makeInitVeroStepSucceeding()

        whenever(scannerManagerSpy) { connectToVero() } thenReturn Completable.error(BluetoothNotEnabledException())
        launchActivityRule.launchActivity(enrolRequest.toIntent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(Alert.BLUETOOTH_NOT_SUPPORTED.title)))
    }

    @Test
    fun bluetoothNotPairedFromConnectVeroStep_shouldAnErrorAlert() {
        makeInitVeroStepSucceeding()

        whenever(scannerManagerSpy) { connectToVero() } thenReturn Completable.error(ScannerNotPairedException())
        launchActivityRule.launchActivity(enrolRequest.toIntent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(Alert.NOT_PAIRED.title)))
    }

    @Test
    fun unknownBluetoothIssueFromConnectVeroStep_shouldAnErrorAlert() {
        makeInitVeroStepSucceeding()

        whenever(scannerManagerSpy) { connectToVero() } thenReturn Completable.error(UnknownBluetoothIssueException())
        launchActivityRule.launchActivity(enrolRequest.toIntent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(Alert.DISCONNECTED.title)))
    }

    @Test
    fun unknownBluetoothIssueFromResetUIVeroStep_shouldAnErrorAlert() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepSucceeding()

        whenever(scannerManagerSpy) { resetVeroUI() } thenReturn Completable.error(UnknownBluetoothIssueException())
        launchActivityRule.launchActivity(enrolRequest.toIntent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(Alert.DISCONNECTED.title)))
    }

    @Test
    fun lowBatteryFromWakingUpVeroStep_shouldAnErrorAlert() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepSucceeding()
        makeResetVeroUISucceeding()

        whenever(scannerManagerSpy) { wakeUpVero() } thenReturn Completable.error(ScannerLowBatteryException())
        launchActivityRule.launchActivity(enrolRequest.toIntent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(Alert.LOW_BATTERY.title)))
    }

    @Test
    fun unknownBluetoothIssueFromWakingUpVeroStep_shouldAnErrorAlert() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepSucceeding()
        makeResetVeroUISucceeding()

        whenever(scannerManagerSpy) { wakeUpVero() } thenReturn Completable.error(UnknownBluetoothIssueException())
        launchActivityRule.launchActivity(enrolRequest.toIntent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(Alert.DISCONNECTED.title)))
    }

    @Test
    fun guidForVerificationNotAvailableLocallyAndPhoneIsOffline_shouldAnErrorAlert() {
        makeSetupVeroSucceeding()

        whenever(dbManagerMock) { loadPerson(anyNotNull(), anyNotNull()) } thenReturn Single.error(IllegalStateException())
        whenever(simNetworkUtilsMock) { isConnected() } thenReturn false

        launchActivityRule.launchActivity(verifyRequest.toIntent())

        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(Alert.GUID_NOT_FOUND_OFFLINE.title)))
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
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(Alert.GUID_NOT_FOUND_ONLINE.title)))
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
    }
}
