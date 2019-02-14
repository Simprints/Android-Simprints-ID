package com.simprints.id.integration.ui

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.collectFingerprints.CollectFingerprintsActivity
import com.simprints.id.activities.collectFingerprints.ViewPagerCustom
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_REALM_KEY
import com.simprints.id.commontesttools.di.DependencyRule
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.scanner.ScannerManager
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.testSnippets.collectFingerprintsPressScan
import com.simprints.id.testSnippets.setupRandomGeneratorToGenerateKey
import com.simprints.id.testSnippets.skipFinger
import com.simprints.id.testSnippets.waitForSplashScreenAppearsAndDisappears
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.id.testtools.ScannerUtils.setupScannerForCollectingFingerprints
import com.simprints.id.testtools.launchCollectFingerprintsActivity
import com.simprints.id.tools.RandomGenerator
import com.simprints.libsimprints.FingerIdentifier
import com.simprints.mockscanner.MockBluetoothAdapter
import com.simprints.mockscanner.MockFinger
import com.simprints.mockscanner.MockScannerManager
import com.simprints.testframework.android.getCurrentActivity
import com.simprints.testframework.common.syntax.whenever
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
class CollectFingerprintsActivityTest {

    private val app = ApplicationProvider.getApplicationContext<Application>()

    @get:Rule val collectFingerprintsRule = ActivityTestRule(CollectFingerprintsActivity::class.java, false, false)

    private val preferencesModule by lazy {
        TestPreferencesModule(settingsPreferencesManagerRule = DependencyRule.SpyRule)
    }

    private val module by lazy {
        TestAppModule(app,
            randomGeneratorRule = DependencyRule.MockRule,
            bluetoothComponentAdapterRule = DependencyRule.ReplaceRule { mockBluetoothAdapter })
    }

    private var mockBluetoothAdapter: MockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager())
    @Inject lateinit var randomGeneratorMock: RandomGenerator
    @Inject lateinit var settingsPreferencesManagerSpy: SettingsPreferencesManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var scannerManager: ScannerManager

    @Before
    fun setUp() {
        AndroidTestConfig(this, module, preferencesModule).fullSetup()

        setupRandomGeneratorToGenerateKey(DEFAULT_REALM_KEY, randomGeneratorMock)

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
