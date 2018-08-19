package com.simprints.id.coreFeatures

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.util.Base64
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.activities.collectFingerprints.ViewPagerCustom
import com.simprints.id.activities.login.LoginActivity
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.RealmConfig
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.di.AppModuleForAndroidTests
import com.simprints.id.di.DaggerForAndroidTests
import com.simprints.id.shared.DependencyRule
import com.simprints.id.shared.PreferencesModuleForAnyTests
import com.simprints.id.shared.whenever
import com.simprints.id.testSnippets.*
import com.simprints.id.testTemplates.FirstUseLocal
import com.simprints.id.testTemplates.HappyWifi
import com.simprints.id.testTools.ActivityUtils.getCurrentActivity
import com.simprints.id.testTools.CalloutCredentials
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.libsimprints.FingerIdentifier
import com.simprints.mockscanner.MockBluetoothAdapter
import com.simprints.mockscanner.MockFinger
import com.simprints.mockscanner.MockScannerManager
import io.realm.Realm
import io.realm.RealmConfiguration
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
class CaptureFingerActivityTest : DaggerForAndroidTests(), FirstUseLocal, HappyWifi {

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
    val scanTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    override var preferencesModule: PreferencesModuleForAnyTests by lazyVar {
        PreferencesModuleForAnyTests(settingsPreferencesManagerRule = DependencyRule.SpyRule)
    }

    override var module by lazyVar {
        AppModuleForAndroidTests(app,
            randomGeneratorRule = DependencyRule.MockRule,
            bluetoothComponentAdapterRule = DependencyRule.ReplaceRule { mockBluetoothAdapter })
    }

    private lateinit var mockBluetoothAdapter: MockBluetoothAdapter
    @Inject lateinit var randomGeneratorMock: RandomGenerator
    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var settingsPreferencesManagerSpy: SettingsPreferencesManager

    @Before
    override fun setUp() {
        super<HappyWifi>.setUp()
        app = InstrumentationRegistry.getTargetContext().applicationContext as Application
        super<DaggerForAndroidTests>.setUp()
        testAppComponent.inject(this)

        setupRandomGeneratorToGenerateKey(realmKey, randomGeneratorMock)

        app.initDependencies()

        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        realmConfiguration = RealmConfig.get(localDbKey.projectId, localDbKey.value, localDbKey.projectId)
        super<FirstUseLocal>.setUp()
    }

    @Test
    fun threeBadScanAndNotMaxReached_thenAddAFinger() {

        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(
            MockFinger.PERSON_1_VERSION_1_LEFT_THUMB_BAD_SCAN,
            MockFinger.PERSON_1_VERSION_1_LEFT_THUMB_BAD_SCAN,
            MockFinger.PERSON_1_VERSION_1_LEFT_THUMB_BAD_SCAN)))

        launchActivityEnrol(calloutCredentials, scanTestRule)

        signInIfRequired()
        setupActivityAndContinue()
        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)

        addBadFingerAndScan()
        addBadFingerAndScan()
        addBadFingerAndScan()

        waitForSplashScreenAppearsAndDisappears().also { Thread.sleep(2000) }

        Assert.assertEquals(3, viewPager?.adapter?.count)
        Assert.assertEquals(1, viewPager?.currentItem)
    }

    @Test
    fun threeBadAndMaxReached_thenNotFingerAdded() {

        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(
            MockFinger.PERSON_1_VERSION_1_LEFT_THUMB_BAD_SCAN,
            MockFinger.PERSON_1_VERSION_1_LEFT_THUMB_BAD_SCAN,
            MockFinger.PERSON_1_VERSION_1_LEFT_THUMB_BAD_SCAN)))

        whenever(settingsPreferencesManagerSpy.fingerStatus).thenReturn(hashMapOf(
            FingerIdentifier.LEFT_THUMB to true,
            FingerIdentifier.LEFT_INDEX_FINGER to true,
            FingerIdentifier.RIGHT_THUMB to true,
            FingerIdentifier.RIGHT_INDEX_FINGER to true))

        launchActivityEnrol(calloutCredentials, scanTestRule)

        signInIfRequired()
        setupActivityAndContinue()
        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)

        Assert.assertEquals(4, viewPager?.adapter?.count)

        addBadFingerAndScan()
        addBadFingerAndScan()
        addBadFingerAndScan()

        waitForSplashScreenAppearsAndDisappears().also { Thread.sleep(2000) }

        Assert.assertEquals(4, viewPager?.adapter?.count)
        Assert.assertEquals(1, viewPager?.currentItem)
    }

    @Test
    fun threeMissingBadScans_thenNotFingerAdded() {

        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(
            MockFinger.NO_FINGER,
            MockFinger.NO_FINGER,
            MockFinger.NO_FINGER,
            MockFinger.NO_FINGER)))

        launchActivityEnrol(calloutCredentials, scanTestRule)

        signInIfRequired()
        setupActivityAndContinue()
        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)

        collectFingerprintsPressScan()
        collectFingerprintsPressScan()
        collectFingerprintsPressScan()
        collectFingerprintsPressScan()

        Assert.assertEquals(2, viewPager?.adapter?.count)
        Assert.assertEquals(0, viewPager?.currentItem)
    }

    private fun addBadFingerAndScan(){
        whenever(settingsPreferencesManagerSpy.qualityThreshold).thenReturn(100)
        collectFingerprintsPressScan()
    }

    private fun signInIfRequired() {
        if (getCurrentActivity() is LoginActivity) {
            enterCredentialsDirectly(calloutCredentials, projectSecret)
            pressSignIn()
        }
    }
}
