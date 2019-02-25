package com.simprints.id.activities.launch

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_REALM_KEY
import com.simprints.id.commontesttools.di.DependencyRule
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.commontesttools.state.mockSettingsPreferencesManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.exceptions.safe.setup.*
import com.simprints.id.exceptions.unexpected.UnknownBluetoothIssueException
import com.simprints.id.scanner.ScannerManager
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.testtools.state.setupRandomGeneratorToGenerateKey
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.libscanner.Scanner
import com.simprints.libsimprints.Constants
import com.simprints.mockscanner.MockBluetoothAdapter
import com.simprints.mockscanner.MockScannerManager
import com.simprints.testtools.android.waitOnUi
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
class LaunchActivityAndroidTest {

    private val app = ApplicationProvider.getApplicationContext<Application>()

    @Inject lateinit var dbManagerSpy: DbManager
    @Inject lateinit var simNetworkUtilsSpy: SimNetworkUtils

    @get:Rule var permissionRule: GrantPermissionRule? = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)
    @get:Rule val launchActivityRule = ActivityTestRule(LaunchActivity::class.java, false, false)

    private val preferencesModule by lazy {
        TestPreferencesModule(settingsPreferencesManagerRule = DependencyRule.SpyRule)
    }

    private val module by lazy {
        TestAppModule(app,
            dbManagerRule = DependencyRule.SpyRule,
            randomGeneratorRule = DependencyRule.MockRule,
            bluetoothComponentAdapterRule = DependencyRule.ReplaceRule { mockBluetoothAdapter },
            scannerManagerRule = DependencyRule.SpyRule,
            simNetworkUtilsRule = DependencyRule.SpyRule,
            syncSchedulerHelperRule = DependencyRule.MockRule)
    }

    private var mockBluetoothAdapter: MockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager())
    @Inject lateinit var randomGeneratorMock: RandomGenerator
    @Inject lateinit var settingsPreferencesManagerSpy: SettingsPreferencesManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var scannerManagerSpy: ScannerManager

    @Before
    fun setUp() {
        AndroidTestConfig(this, module, preferencesModule).fullSetup()

        setupRandomGeneratorToGenerateKey(DEFAULT_REALM_KEY, randomGeneratorMock)

        preferencesManager.calloutAction = CalloutAction.REGISTER
    }

    @Test
    fun notScannerFromInitVeroStep_shouldAnErrorAlert() {
        mockSettingsPreferencesManager(settingsPreferencesManagerSpy, parentalConsentExists = false, generalConsentOptions = REMOTE_CONSENT_GENERAL_OPTIONS)
        whenever(scannerManagerSpy) { initVero()} thenReturn Completable.error(ScannerNotPairedException())
        launchActivityRule.launchActivity(Intent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(ALERT_TYPE.NOT_PAIRED.alertTitleId)))
    }

    @Test
    fun multiScannersPairedFromInitVeroStep_shouldAnErrorAlert() {
        mockSettingsPreferencesManager(settingsPreferencesManagerSpy, parentalConsentExists = false, generalConsentOptions = REMOTE_CONSENT_GENERAL_OPTIONS)
        whenever(scannerManagerSpy) { initVero() } thenReturn Completable.error(MultipleScannersPairedException())
        launchActivityRule.launchActivity(Intent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(ALERT_TYPE.MULTIPLE_PAIRED_SCANNERS.alertTitleId)))
    }

    @Test
    fun bluetoothOffFromConnectVeroStep_shouldAnErrorAlert() {
        mockSettingsPreferencesManager(settingsPreferencesManagerSpy, parentalConsentExists = false, generalConsentOptions = REMOTE_CONSENT_GENERAL_OPTIONS)
        makeInitVeroStepSucceeding()

        whenever(scannerManagerSpy) { connectToVero() } thenReturn Completable.error(BluetoothNotEnabledException())
        launchActivityRule.launchActivity(Intent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(ALERT_TYPE.BLUETOOTH_NOT_ENABLED.alertTitleId)))
    }

    @Test
    fun bluetoothNotSupportedFromConnectVeroStep_shouldAnErrorAlert() {
        mockSettingsPreferencesManager(settingsPreferencesManagerSpy, parentalConsentExists = false, generalConsentOptions = REMOTE_CONSENT_GENERAL_OPTIONS)
        makeInitVeroStepSucceeding()

        whenever(scannerManagerSpy) { connectToVero() } thenReturn Completable.error(BluetoothNotEnabledException())
        launchActivityRule.launchActivity(Intent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(ALERT_TYPE.BLUETOOTH_NOT_SUPPORTED.alertTitleId)))
    }

    @Test
    fun bluetoothNotPairedFromConnectVeroStep_shouldAnErrorAlert() {
        mockSettingsPreferencesManager(settingsPreferencesManagerSpy, parentalConsentExists = false, generalConsentOptions = REMOTE_CONSENT_GENERAL_OPTIONS)
        makeInitVeroStepSucceeding()

        whenever(scannerManagerSpy) { connectToVero() } thenReturn Completable.error(ScannerNotPairedException())
        launchActivityRule.launchActivity(Intent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(ALERT_TYPE.NOT_PAIRED.alertTitleId)))
    }

    @Test
    fun unknownBluetoothIssueFromConnectVeroStep_shouldAnErrorAlert() {
        mockSettingsPreferencesManager(settingsPreferencesManagerSpy, parentalConsentExists = false, generalConsentOptions = REMOTE_CONSENT_GENERAL_OPTIONS)
        makeInitVeroStepSucceeding()

        whenever(scannerManagerSpy) { connectToVero() } thenReturn Completable.error(UnknownBluetoothIssueException())
        launchActivityRule.launchActivity(Intent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(ALERT_TYPE.DISCONNECTED.alertTitleId)))
    }

    @Test
    fun unknownBluetoothIssueFromResetUIVeroStep_shouldAnErrorAlert() {
        mockSettingsPreferencesManager(settingsPreferencesManagerSpy, parentalConsentExists = false, generalConsentOptions = REMOTE_CONSENT_GENERAL_OPTIONS)
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepSucceeding()

        whenever(scannerManagerSpy) { resetVeroUI() } thenReturn Completable.error(UnknownBluetoothIssueException())
        launchActivityRule.launchActivity(Intent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(ALERT_TYPE.DISCONNECTED.alertTitleId)))
    }

    @Test
    fun lowBatteryFromWakingUpVeroStep_shouldAnErrorAlert() {
        mockSettingsPreferencesManager(settingsPreferencesManagerSpy, parentalConsentExists = false, generalConsentOptions = REMOTE_CONSENT_GENERAL_OPTIONS)
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepSucceeding()
        makeResetVeroUISucceeding()

        whenever(scannerManagerSpy) { wakeUpVero() } thenReturn Completable.error(ScannerLowBatteryException())
        launchActivityRule.launchActivity(Intent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(ALERT_TYPE.LOW_BATTERY.alertTitleId)))
    }

    @Test
    fun unknownBluetoothIssueFromWakingUpVeroStep_shouldAnErrorAlert() {
        mockSettingsPreferencesManager(settingsPreferencesManagerSpy, parentalConsentExists = false, generalConsentOptions = REMOTE_CONSENT_GENERAL_OPTIONS)
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepSucceeding()
        makeResetVeroUISucceeding()

        whenever(scannerManagerSpy) { wakeUpVero() } thenReturn Completable.error(UnknownBluetoothIssueException())
        launchActivityRule.launchActivity(Intent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(ALERT_TYPE.DISCONNECTED.alertTitleId)))
    }

    @Test
    fun guidForVerificationNotAvailableLocallyAndPhoneIsOffline_shouldAnErrorAlert() {
        mockSettingsPreferencesManager(settingsPreferencesManagerSpy, parentalConsentExists = false, generalConsentOptions = REMOTE_CONSENT_GENERAL_OPTIONS)
        makeSetupVeroSucceeding()
        preferencesManager.calloutAction = CalloutAction.VERIFY

        whenever(dbManagerSpy) { loadPerson(anyNotNull(), anyNotNull()) } thenReturn Single.error(IllegalStateException())
        whenever(simNetworkUtilsSpy) { isConnected() } thenReturn false

        launchActivityRule.launchActivity(Intent())

        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(ALERT_TYPE.GUID_NOT_FOUND_OFFLINE.alertTitleId)))
    }

    @Test
    fun guidForVerificationNotAvailableLocallyAndPhoneIsOnline_shouldAnErrorAlert() {
        mockSettingsPreferencesManager(settingsPreferencesManagerSpy, parentalConsentExists = false, generalConsentOptions = REMOTE_CONSENT_GENERAL_OPTIONS)
        makeSetupVeroSucceeding()
        preferencesManager.calloutAction = CalloutAction.VERIFY

        whenever(dbManagerSpy) { loadPerson(anyNotNull(), anyNotNull()) } thenReturn Single.error(IllegalStateException())
        whenever(simNetworkUtilsSpy) { isConnected() } thenReturn true

        launchActivityRule.launchActivity(Intent().also { it.action = Constants.SIMPRINTS_VERIFY_INTENT })
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(ALERT_TYPE.GUID_NOT_FOUND_ONLINE.alertTitleId)))
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
        private const val MAC_ADDRESS = "F0:AC:D7:C8:CB:22"
        private const val REMOTE_CONSENT_GENERAL_OPTIONS = "{\"consent_enrol_only\":false,\"consent_enrol\":true,\"consent_id_verify\":true,\"consent_share_data_no\":true,\"consent_share_data_yes\":false,\"consent_collect_yes\":false,\"consent_privacy_rights\":true,\"consent_confirmation\":true}"
    }
}
