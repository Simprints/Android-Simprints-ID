package com.simprints.fingerprint.activities.collectfingerprint

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.collect.CollectFingerprintsActivity
import com.simprints.fingerprint.activities.collect.ViewPagerCustom
import com.simprints.fingerprint.activities.collect.models.FingerIdentifier
import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsTaskRequest
import com.simprints.fingerprint.commontesttools.di.TestFingerprintModule
import com.simprints.fingerprint.controllers.scanner.ScannerManager
import com.simprints.fingerprint.data.domain.Action
import com.simprints.fingerprint.testtools.AndroidTestConfig
import com.simprints.fingerprint.testtools.ScannerUtils.setupScannerForCollectingFingerprints
import com.simprints.fingerprint.testtools.collectFingerprintsPressScan
import com.simprints.fingerprint.testtools.skipFinger
import com.simprints.fingerprint.testtools.waitForSplashScreenAppearsAndDisappears
import com.simprints.fingerprintscannermock.MockBluetoothAdapter
import com.simprints.fingerprintscannermock.MockFinger
import com.simprints.fingerprintscannermock.MockScannerManager
import com.simprints.testtools.android.getCurrentActivity
import com.simprints.testtools.common.di.DependencyRule
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@MediumTest
@RunWith(AndroidJUnit4::class)
class CollectFingerprintsActivityTest {

    @get:Rule val collectFingerprintsRule = ActivityTestRule(CollectFingerprintsActivity::class.java, false, false)

    private var mockBluetoothAdapter: MockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager())
    @Inject lateinit var scannerManager: ScannerManager

    private val fingerprintModule by lazy {
        TestFingerprintModule(
            bluetoothComponentAdapter = DependencyRule.ReplaceRule { mockBluetoothAdapter })
    }

    @Before
    fun setUp() {
        AndroidTestConfig(this, fingerprintModule).fullSetup()
    }

    @Test
    fun threeBadScanAndMaxNotReached_shouldAddAFinger() {
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(
            MockFinger.PERSON_1_VERSION_1_LEFT_THUMB_BAD_SCAN,
            MockFinger.PERSON_1_VERSION_1_LEFT_THUMB_BAD_SCAN,
            MockFinger.PERSON_1_VERSION_1_LEFT_THUMB_BAD_SCAN)))
        setupScannerForCollectingFingerprints(mockBluetoothAdapter, scannerManager)
        collectFingerprintsRule.launchActivity(collectTaskRequest(Action.ENROL, FINGER_STATUS_TWO_FINGERS).toIntent())

        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)

        collectFingerprintsPressScan()
        collectFingerprintsPressScan()
        collectFingerprintsPressScan()

        waitForSplashScreenAppearsAndDisappears()

        assertEquals(3, viewPager?.adapter?.count)
        assertEquals(1, viewPager?.currentItem)
    }

    @Test
    fun threeBadAndMaxReached_shouldNotAddAFinger() {
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(
            MockFinger.PERSON_1_VERSION_1_LEFT_THUMB_BAD_SCAN,
            MockFinger.PERSON_1_VERSION_1_LEFT_THUMB_BAD_SCAN,
            MockFinger.PERSON_1_VERSION_1_LEFT_THUMB_BAD_SCAN)))
        setupScannerForCollectingFingerprints(mockBluetoothAdapter, scannerManager)
        collectFingerprintsRule.launchActivity(collectTaskRequest(Action.ENROL, FINGER_STATUS_FOUR_FINGERS).toIntent())

        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)

        assertEquals(4, viewPager?.adapter?.count)

        collectFingerprintsPressScan()
        collectFingerprintsPressScan()
        collectFingerprintsPressScan()

        waitForSplashScreenAppearsAndDisappears()

        assertEquals(4, viewPager?.adapter?.count)
        assertEquals(1, viewPager?.currentItem)
    }

    @Test
    fun threeBadScansDueToMissingTemplates_shouldNotAddAFinger() {
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(
            MockFinger.NO_FINGER,
            MockFinger.NO_FINGER,
            MockFinger.NO_FINGER)))
        setupScannerForCollectingFingerprints(mockBluetoothAdapter, scannerManager)
        collectFingerprintsRule.launchActivity(collectTaskRequest(Action.ENROL, FINGER_STATUS_TWO_FINGERS).toIntent())

        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)

        collectFingerprintsPressScan()
        collectFingerprintsPressScan()
        collectFingerprintsPressScan()

        assertEquals(2, viewPager?.adapter?.count)
        assertEquals(0, viewPager?.currentItem)
    }

    @Test
    fun skipFingerAndMaxNotReached_shouldAddAFinger() {
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(MockFinger.NO_FINGER)))
        setupScannerForCollectingFingerprints(mockBluetoothAdapter, scannerManager)
        collectFingerprintsRule.launchActivity(collectTaskRequest(Action.ENROL, FINGER_STATUS_TWO_FINGERS).toIntent())

        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)

        skipFinger()

        waitForSplashScreenAppearsAndDisappears()

        assertEquals(3, viewPager?.adapter?.count)
        assertEquals(1, viewPager?.currentItem)
    }

    @Test
    fun skipFingerAndMaxReached_shouldNotAddAFinger() {
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(MockFinger.NO_FINGER)))
        setupScannerForCollectingFingerprints(mockBluetoothAdapter, scannerManager)
        collectFingerprintsRule.launchActivity(collectTaskRequest(Action.ENROL, FINGER_STATUS_FOUR_FINGERS).toIntent())

        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)

        skipFinger()

        waitForSplashScreenAppearsAndDisappears()
        assertEquals(4, viewPager?.adapter?.count)
        assertEquals(1, viewPager?.currentItem)
    }

    companion object {
        private const val DEFAULT_PROJECT_ID = "some_project_id"
        private const val DEFAULT_USER_ID = "some_user_id"
        private const val DEFAULT_MODULE_ID = "some_module_id"
        private const val DEFAULT_LANGUAGE = "en"
        private val FINGER_STATUS_TWO_FINGERS = mapOf(
            FingerIdentifier.RIGHT_THUMB to false,
            FingerIdentifier.RIGHT_INDEX_FINGER to false,
            FingerIdentifier.RIGHT_3RD_FINGER to false,
            FingerIdentifier.RIGHT_4TH_FINGER to false,
            FingerIdentifier.RIGHT_5TH_FINGER to false,
            FingerIdentifier.LEFT_THUMB to true,
            FingerIdentifier.LEFT_INDEX_FINGER to true,
            FingerIdentifier.LEFT_3RD_FINGER to false,
            FingerIdentifier.LEFT_4TH_FINGER to false,
            FingerIdentifier.LEFT_5TH_FINGER to false
        )
        private val FINGER_STATUS_FOUR_FINGERS = mapOf(
            FingerIdentifier.RIGHT_THUMB to true,
            FingerIdentifier.RIGHT_INDEX_FINGER to true,
            FingerIdentifier.RIGHT_3RD_FINGER to false,
            FingerIdentifier.RIGHT_4TH_FINGER to false,
            FingerIdentifier.RIGHT_5TH_FINGER to false,
            FingerIdentifier.LEFT_THUMB to true,
            FingerIdentifier.LEFT_INDEX_FINGER to true,
            FingerIdentifier.LEFT_3RD_FINGER to false,
            FingerIdentifier.LEFT_4TH_FINGER to false,
            FingerIdentifier.LEFT_5TH_FINGER to false
        )

        private fun collectTaskRequest(action: Action, fingerStatus: Map<FingerIdentifier, Boolean>) =
            CollectFingerprintsTaskRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID,
                action, DEFAULT_LANGUAGE, fingerStatus)

        private fun CollectFingerprintsTaskRequest.toIntent() = Intent().also {
            it.putExtra(CollectFingerprintsTaskRequest.BUNDLE_KEY, this)
        }
    }
}
