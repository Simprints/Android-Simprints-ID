package com.simprints.id.activities

import android.content.Intent
import androidx.test.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import com.nhaarman.mockito_kotlin.doReturn
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.launch.LaunchActivity
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.di.AppModuleForAndroidTests
import com.simprints.id.di.DaggerForAndroidTests
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.exceptions.safe.setup.*
import com.simprints.id.scanner.ScannerManager
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.shared.DefaultTestConstants.DEFAULT_REALM_KEY
import com.simprints.id.shared.DependencyRule
import com.simprints.id.shared.PreferencesModuleForAnyTests
import com.simprints.id.shared.anyNotNull
import com.simprints.id.shared.mockSettingsPreferencesManager
import com.simprints.id.testSnippets.setupRandomGeneratorToGenerateKey
import com.simprints.id.testTemplates.FirstUseLocal
import com.simprints.id.testTools.waitOnUi
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.libcommon.Person
import com.simprints.libscanner.Scanner
import com.simprints.libsimprints.Constants
import com.simprints.mockscanner.MockBluetoothAdapter
import com.simprints.mockscanner.MockScannerManager
import io.reactivex.Completable
import io.reactivex.Single
import io.realm.Realm
import io.realm.RealmConfiguration
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
class LaunchActivityAndroidTest : DaggerForAndroidTests(), FirstUseLocal {

    @Inject lateinit var dbManagerSpy: DbManager
    @Inject lateinit var simNetworkUtilsSpy: SimNetworkUtils

    override var peopleRealmConfiguration: RealmConfiguration? = null
    override var sessionsRealmConfiguration: RealmConfiguration? = null

    @Rule @JvmField var permissionRule: GrantPermissionRule? = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)
    @Rule @JvmField val launchActivityRule = ActivityTestRule(LaunchActivity::class.java, false, false)

    override var preferencesModule: PreferencesModuleForAnyTests by lazyVar {
        PreferencesModuleForAnyTests(settingsPreferencesManagerRule = DependencyRule.SpyRule)
    }

    override var module by lazyVar {
        AppModuleForAndroidTests(app,
            dbManagerRule = DependencyRule.SpyRule,
            randomGeneratorRule = DependencyRule.MockRule,
            bluetoothComponentAdapterRule = DependencyRule.ReplaceRule { mockBluetoothAdapter },
            scannerManagerRule = DependencyRule.SpyRule,
            simNetworkUtilsRule = DependencyRule.SpyRule)
    }

    private var mockBluetoothAdapter: MockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager())
    @Inject lateinit var randomGeneratorMock: RandomGenerator
    @Inject lateinit var settingsPreferencesManagerSpy: SettingsPreferencesManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var scannerManagerSpy: ScannerManager

    @Before
    override fun setUp() {
        app = InstrumentationRegistry.getTargetContext().applicationContext as Application
        super<DaggerForAndroidTests>.setUp()
        testAppComponent.inject(this)

        setupRandomGeneratorToGenerateKey(DEFAULT_REALM_KEY, randomGeneratorMock)

        app.initDependencies()

        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        peopleRealmConfiguration = FirstUseLocal.defaultPeopleRealmConfiguration
        sessionsRealmConfiguration = FirstUseLocal.defaultSessionRealmConfiguration
        super<FirstUseLocal>.setUp()

        preferencesManager.calloutAction = CalloutAction.REGISTER
    }

    @Test
    fun notScannerFromInitVeroStep_shouldAnErrorAlert() {
        mockSettingsPreferencesManager(settingsPreferencesManagerSpy, parentalConsentExists = false, generalConsentOptions = REMOTE_CONSENT_GENERAL_OPTIONS)
        doReturn(Completable.error(ScannerNotPairedException())).`when`(scannerManagerSpy).initVero()
        launchActivityRule.launchActivity(Intent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(ALERT_TYPE.NOT_PAIRED.alertTitleId)))
    }

    @Test
    fun multiScannersPairedFromInitVeroStep_shouldAnErrorAlert() {
        mockSettingsPreferencesManager(settingsPreferencesManagerSpy, parentalConsentExists = false, generalConsentOptions = REMOTE_CONSENT_GENERAL_OPTIONS)
        doReturn(Completable.error(MultipleScannersPairedException())).`when`(scannerManagerSpy).initVero()
        launchActivityRule.launchActivity(Intent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(ALERT_TYPE.MULTIPLE_PAIRED_SCANNERS.alertTitleId)))
    }

    @Test
    fun bluetoothOffFromConnectVeroStep_shouldAnErrorAlert() {
        mockSettingsPreferencesManager(settingsPreferencesManagerSpy, parentalConsentExists = false, generalConsentOptions = REMOTE_CONSENT_GENERAL_OPTIONS)
        makeInitVeroStepSucceeding()

        doReturn(Completable.error(BluetoothNotEnabledException())).`when`(scannerManagerSpy).connectToVero()
        launchActivityRule.launchActivity(Intent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(ALERT_TYPE.BLUETOOTH_NOT_ENABLED.alertTitleId)))
    }

    @Test
    fun bluetoothNotSupportedFromConnectVeroStep_shouldAnErrorAlert() {
        mockSettingsPreferencesManager(settingsPreferencesManagerSpy, parentalConsentExists = false, generalConsentOptions = REMOTE_CONSENT_GENERAL_OPTIONS)
        makeInitVeroStepSucceeding()

        doReturn(Completable.error(BluetoothNotEnabledException())).`when`(scannerManagerSpy).connectToVero()
        launchActivityRule.launchActivity(Intent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(ALERT_TYPE.BLUETOOTH_NOT_SUPPORTED.alertTitleId)))
    }

    @Test
    fun bluetoothNotPairedFromConnectVeroStep_shouldAnErrorAlert() {
        mockSettingsPreferencesManager(settingsPreferencesManagerSpy, parentalConsentExists = false, generalConsentOptions = REMOTE_CONSENT_GENERAL_OPTIONS)
        makeInitVeroStepSucceeding()

        doReturn(Completable.error(ScannerNotPairedException())).`when`(scannerManagerSpy).connectToVero()
        launchActivityRule.launchActivity(Intent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(ALERT_TYPE.NOT_PAIRED.alertTitleId)))
    }

    @Test
    fun unknownBluetoothIssueFromConnectVeroStep_shouldAnErrorAlert() {
        mockSettingsPreferencesManager(settingsPreferencesManagerSpy, parentalConsentExists = false, generalConsentOptions = REMOTE_CONSENT_GENERAL_OPTIONS)
        makeInitVeroStepSucceeding()

        doReturn(Completable.error(UnknownBluetoothIssueException())).`when`(scannerManagerSpy).connectToVero()
        launchActivityRule.launchActivity(Intent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(ALERT_TYPE.DISCONNECTED.alertTitleId)))
    }

    @Test
    fun unknownBluetoothIssueFromResetUIVeroStep_shouldAnErrorAlert() {
        mockSettingsPreferencesManager(settingsPreferencesManagerSpy, parentalConsentExists = false, generalConsentOptions = REMOTE_CONSENT_GENERAL_OPTIONS)
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepSucceeding()

        doReturn(Completable.error(UnknownBluetoothIssueException())).`when`(scannerManagerSpy).resetVeroUI()
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

        doReturn(Completable.error(ScannerLowBatteryException())).`when`(scannerManagerSpy).wakingUpVero()
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

        doReturn(Completable.error(UnknownBluetoothIssueException())).`when`(scannerManagerSpy).wakingUpVero()
        launchActivityRule.launchActivity(Intent())
        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(ALERT_TYPE.DISCONNECTED.alertTitleId)))
    }

    @Test
    fun guidForVerificationNotAvailableLocallyAndPhoneIsOffline_shouldAnErrorAlert() {
        mockSettingsPreferencesManager(settingsPreferencesManagerSpy, parentalConsentExists = false, generalConsentOptions = REMOTE_CONSENT_GENERAL_OPTIONS)
        makeSetupVeroSucceeding()
        preferencesManager.calloutAction = CalloutAction.VERIFY

        doReturn(Single.create<Person> { it.onError(IllegalStateException()) }).`when`(dbManagerSpy).loadPerson(anyNotNull(), anyNotNull())
        doReturn(false).`when`(simNetworkUtilsSpy).isConnected()

        launchActivityRule.launchActivity(Intent())

        waitOnUi(1000)
        onView(withId(R.id.alert_title)).check(ViewAssertions.matches(withText(ALERT_TYPE.GUID_NOT_FOUND_OFFLINE.alertTitleId)))
    }

    @Test
    fun guidForVerificationNotAvailableLocallyAndPhoneIsOnline_shouldAnErrorAlert() {
        mockSettingsPreferencesManager(settingsPreferencesManagerSpy, parentalConsentExists = false, generalConsentOptions = REMOTE_CONSENT_GENERAL_OPTIONS)
        makeSetupVeroSucceeding()
        preferencesManager.calloutAction = CalloutAction.VERIFY

        doReturn(Single.create<Person> { it.onError(IllegalStateException()) }).`when`(dbManagerSpy).loadPerson(anyNotNull(), anyNotNull())
        doReturn(true).`when`(simNetworkUtilsSpy).isConnected()

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
        doReturn(Completable.complete()).`when`(scannerManagerSpy).resetVeroUI()
    }

    private fun makeConnectToVeroStepSucceeding() {
        doReturn(Completable.complete()).`when`(scannerManagerSpy).connectToVero()
    }

    private fun makeWakingUpVeroStepSucceeding() {
        doReturn(Completable.complete()).`when`(scannerManagerSpy).wakingUpVero()
    }

    private fun makeInitVeroStepSucceeding() {
        doReturn(Completable.complete()).`when`(scannerManagerSpy).initVero()
        scannerManagerSpy.scanner = Scanner(MAC_ADDRESS, mockBluetoothAdapter)
    }

    companion object {
        private const val MAC_ADDRESS = "F0:AC:D7:C8:CB:22"
        private const val REMOTE_CONSENT_GENERAL_OPTIONS = "{\"consent_enrol_only\":false,\"consent_enrol\":true,\"consent_id_verify\":true,\"consent_share_data_no\":true,\"consent_share_data_yes\":false,\"consent_collect_yes\":false,\"consent_privacy_rights\":true,\"consent_confirmation\":true}"
    }
}
