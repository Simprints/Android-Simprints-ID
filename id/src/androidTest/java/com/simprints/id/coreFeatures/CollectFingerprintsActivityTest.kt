package com.simprints.id.coreFeatures

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.util.Base64
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.collectFingerprints.CollectFingerprintsActivity
import com.simprints.id.activities.collectFingerprints.ViewPagerCustom
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.PeopleRealmConfig
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.di.AppModuleForAndroidTests
import com.simprints.id.di.DaggerForAndroidTests
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.shared.DependencyRule
import com.simprints.id.shared.PreferencesModuleForAnyTests
import com.simprints.id.shared.whenever
import com.simprints.id.testSnippets.collectFingerprintsPressScan
import com.simprints.id.testSnippets.setupRandomGeneratorToGenerateKey
import com.simprints.id.testSnippets.skipFinger
import com.simprints.id.testSnippets.waitForSplashScreenAppearsAndDisappears
import com.simprints.id.testTemplates.FirstUseLocal
import com.simprints.id.testTools.ActivityUtils.getCurrentActivity
import com.simprints.id.testTools.ActivityUtils.launchCollectFingerprintsActivity
import com.simprints.id.testTools.CalloutCredentials
import com.simprints.id.testTools.ScannerUtils.setupScannerForCollectingFingerprints
import com.simprints.id.scanner.ScannerManager
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
class CollectFingerprintsActivityTest : DaggerForAndroidTests(), FirstUseLocal {

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

    override var peopleRealmConfiguration: RealmConfiguration? = null

    @Rule
    @JvmField
    val collectFingerprintsRule = ActivityTestRule(CollectFingerprintsActivity::class.java, false, false)

    override var preferencesModule: PreferencesModuleForAnyTests by lazyVar {
        PreferencesModuleForAnyTests(settingsPreferencesManagerRule = DependencyRule.SpyRule)
    }

    override var module by lazyVar {
        AppModuleForAndroidTests(app,
            randomGeneratorRule = DependencyRule.MockRule,
            bluetoothComponentAdapterRule = DependencyRule.ReplaceRule { mockBluetoothAdapter })
    }

    private var mockBluetoothAdapter: MockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager())
    @Inject lateinit var randomGeneratorMock: RandomGenerator
    @Inject lateinit var settingsPreferencesManagerSpy: SettingsPreferencesManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var scannerManager: ScannerManager

    @Before
    override fun setUp() {
        app = InstrumentationRegistry.getTargetContext().applicationContext as Application
        super<DaggerForAndroidTests>.setUp()
        testAppComponent.inject(this)

        setupRandomGeneratorToGenerateKey(realmKey, randomGeneratorMock)

        app.initDependencies()

        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        peopleRealmConfiguration = PeopleRealmConfig.get(localDbKey.projectId, localDbKey.value, localDbKey.projectId)
        super<FirstUseLocal>.setUp()

        preferencesManager.calloutAction = CalloutAction.REGISTER
    }

    @Test
    fun threeBadScanAndMaxNotReached_shouldAddAFinger() {
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(
            MockFinger.PERSON_1_VERSION_1_LEFT_THUMB_BAD_SCAN,
            MockFinger.PERSON_1_VERSION_1_LEFT_THUMB_BAD_SCAN,
            MockFinger.PERSON_1_VERSION_1_LEFT_THUMB_BAD_SCAN)))
        setupScannerForCollectingFingerprints(mockBluetoothAdapter, scannerManager)
        launchCollectFingerprintsActivity(collectFingerprintsRule)

        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)

        collectFingerprintsPressScan()
        collectFingerprintsPressScan()
        collectFingerprintsPressScan()

        waitForSplashScreenAppearsAndDisappears()

        Assert.assertEquals(3, viewPager?.adapter?.count)
        Assert.assertEquals(1, viewPager?.currentItem)
    }

    @Test
    fun threeBadAndMaxReached_shouldNotAddAFinger() {
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(
            MockFinger.PERSON_1_VERSION_1_LEFT_THUMB_BAD_SCAN,
            MockFinger.PERSON_1_VERSION_1_LEFT_THUMB_BAD_SCAN,
            MockFinger.PERSON_1_VERSION_1_LEFT_THUMB_BAD_SCAN)))
        setupScannerForCollectingFingerprints(mockBluetoothAdapter, scannerManager)
        setupToCollectFourFingerprints()
        launchCollectFingerprintsActivity(collectFingerprintsRule)

        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)

        Assert.assertEquals(4, viewPager?.adapter?.count)

        collectFingerprintsPressScan()
        collectFingerprintsPressScan()
        collectFingerprintsPressScan()

        waitForSplashScreenAppearsAndDisappears()

        Assert.assertEquals(4, viewPager?.adapter?.count)
        Assert.assertEquals(1, viewPager?.currentItem)
    }

    @Test
    fun threeBadScansDueToMissingTemplates_shouldNotAddAFinger() {
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(
            MockFinger.NO_FINGER,
            MockFinger.NO_FINGER,
            MockFinger.NO_FINGER,
            MockFinger.NO_FINGER)))
        setupScannerForCollectingFingerprints(mockBluetoothAdapter, scannerManager)
        launchCollectFingerprintsActivity(collectFingerprintsRule)

        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)

        collectFingerprintsPressScan()
        collectFingerprintsPressScan()
        collectFingerprintsPressScan()
        collectFingerprintsPressScan()

        Assert.assertEquals(2, viewPager?.adapter?.count)
        Assert.assertEquals(0, viewPager?.currentItem)
    }

    @Test
    fun skipFingerAndMaxNotReached_shouldAddAFinger() {
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(MockFinger.NO_FINGER)))
        setupScannerForCollectingFingerprints(mockBluetoothAdapter, scannerManager)
        launchCollectFingerprintsActivity(collectFingerprintsRule)

        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)

        skipFinger()

        waitForSplashScreenAppearsAndDisappears()

        Assert.assertEquals(3, viewPager?.adapter?.count)
        Assert.assertEquals(1, viewPager?.currentItem)
    }

    @Test
    fun skipFingerAndMaxReached_shouldNotAddAFinger() {
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(MockFinger.NO_FINGER)))
        setupScannerForCollectingFingerprints(mockBluetoothAdapter, scannerManager)
        setupToCollectFourFingerprints()
        launchCollectFingerprintsActivity(collectFingerprintsRule)

        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)

        skipFinger()

        waitForSplashScreenAppearsAndDisappears()
        Assert.assertEquals(4, viewPager?.adapter?.count)
        Assert.assertEquals(1, viewPager?.currentItem)
    }

    private fun setupToCollectFourFingerprints() {
        whenever(settingsPreferencesManagerSpy.fingerStatus).thenReturn(hashMapOf(
            FingerIdentifier.LEFT_THUMB to true,
            FingerIdentifier.LEFT_INDEX_FINGER to true,
            FingerIdentifier.RIGHT_THUMB to true,
            FingerIdentifier.RIGHT_INDEX_FINGER to true))
    }
}
