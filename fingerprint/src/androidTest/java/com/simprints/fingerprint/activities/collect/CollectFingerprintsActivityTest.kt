package com.simprints.fingerprint.activities.collect

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsTaskRequest
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.commontesttools.scanner.*
import com.simprints.fingerprint.controllers.core.flow.Action
import com.simprints.fingerprint.controllers.core.flow.MasterFlowManager
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import com.simprints.fingerprint.di.KoinInjector.acquireFingerprintKoinModules
import com.simprints.fingerprint.di.KoinInjector.releaseFingerprintKoinModules
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.ScannerManagerImpl
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapperV1
import com.simprints.fingerprintscannermock.dummy.DummyBluetoothAdapter
import com.simprints.id.Application
import com.simprints.testtools.android.getCurrentActivity
import com.simprints.testtools.common.syntax.failTest
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.setupMock
import com.simprints.testtools.common.syntax.whenThis
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.mock.declare

@RunWith(AndroidJUnit4::class)
class CollectFingerprintsActivityTest : KoinTest {

    private lateinit var scenario: ActivityScenario<CollectFingerprintsActivity>

    @Before
    fun setUp() {
        acquireFingerprintKoinModules()
    }

    private fun mockScannerManagerWithScanner(scanner: ScannerWrapper) {
        declare {
            factory<ScannerManager> {
                ScannerManagerImpl(DummyBluetoothAdapter(), mock()).also { it.scanner = scanner }
            }
            factory<MasterFlowManager> { setupMock { whenThis { getCurrentAction() } thenReturn Action.IDENTIFY } }
        }
    }

    @Test
    fun twoGoodScansAndThenConfirm_finishesWithCorrectResult() {
        mockScannerManagerWithScanner(ScannerWrapperV1(createMockedScannerV1()))

        scenario = ActivityScenario.launch(collectTaskRequest(TWO_FINGERS).toIntent())

        pressScan()
        pressScan()
        checkIfDialogIsDisplayedWithResultAndClickConfirm()

        val result = scenario.result.resultData.run {
            setExtrasClassLoader(CollectFingerprintsTaskResult::class.java.classLoader)
            extras?.getParcelable<CollectFingerprintsTaskResult>(CollectFingerprintsTaskResult.BUNDLE_KEY)
        }

        assertNotNull(result)
        assertEquals(2, result?.fingerprints?.size)
    }

    @Test
    fun twoGoodScansAndThenRestart_restartsToBeginning() {
        mockScannerManagerWithScanner(ScannerWrapperV1(createMockedScannerV1()))

        scenario = ActivityScenario.launch(collectTaskRequest(TWO_FINGERS).toIntent())

        pressScan()
        pressScan()
        checkIfDialogIsDisplayedAndClickRestart()
        checkFirstFingerYetToBeScanned()

        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)
        assertEquals(2, viewPager?.adapter?.count)
        assertEquals(0, viewPager?.currentItem)
    }

    @Test
    fun mixedScansAndThenConfirm_finishesWithCorrectResult() {
        val scanner = createMockedScannerV1()
        mockScannerManagerWithScanner(ScannerWrapperV1(scanner))

        scenario = ActivityScenario.launch(collectTaskRequest(TWO_FINGERS).toIntent())

        // 1. Good scan
        scanner.queueGoodFinger(FingerIdentifier.LEFT_THUMB)
        pressScan()

        // 2. Three bad scans
        scanner.queueBadFinger(FingerIdentifier.LEFT_INDEX_FINGER)
        pressScan()
        pressScan()
        pressScan()
        waitForSplashScreenToAppearAndDisappear()

        // 3. Finger skipped
        skipFinger()
        waitForSplashScreenToAppearAndDisappear()

        // 4. Finger could not be acquired on sensor, two bad scans, and then a good scan
        scanner.queueFingerNotDetected()
        pressScan()
        scanner.queueBadFinger(FingerIdentifier.RIGHT_INDEX_FINGER)
        pressScan()
        pressScan()
        scanner.queueGoodFinger(FingerIdentifier.RIGHT_INDEX_FINGER)
        pressScan()

        checkIfDialogIsDisplayedWithResultAndClickConfirm("✓ LEFT THUMB\n× LEFT INDEX FINGER\n× RIGHT THUMB\n✓ RIGHT INDEX FINGER\n")

        val result = scenario.result.resultData.run {
            setExtrasClassLoader(CollectFingerprintsTaskResult::class.java.classLoader)
            extras?.getParcelable<CollectFingerprintsTaskResult>(CollectFingerprintsTaskResult.BUNDLE_KEY)
        }

        assertNotNull(result)
        result?.fingerprints?.let {
            assertEquals(3, it.size)
            assertEquals(DEFAULT_GOOD_IMAGE_QUALITY, it[0].qualityScore)
            assertEquals(FingerIdentifier.LEFT_THUMB, it[0].fingerId)
            assertEquals(DEFAULT_BAD_IMAGE_QUALITY, it[1].qualityScore)
            assertEquals(FingerIdentifier.LEFT_INDEX_FINGER, it[1].fingerId)
            assertEquals(DEFAULT_GOOD_IMAGE_QUALITY, it[2].qualityScore)
            assertEquals(FingerIdentifier.RIGHT_INDEX_FINGER, it[2].fingerId)
        }
    }

    @Test
    fun onlySkippedFingers_pressConfirm_notAllowedToContinue() {
        mockScannerManagerWithScanner(ScannerWrapperV1(createMockedScannerV1()))

        scenario = ActivityScenario.launch(collectTaskRequest(TWO_FINGERS).toIntent())

        skipFinger()
        waitForSplashScreenToAppearAndDisappear()

        skipFinger()
        waitForSplashScreenToAppearAndDisappear()

        skipFinger()
        waitForSplashScreenToAppearAndDisappear()

        skipFinger()

        checkIfDialogIsDisplayedWithResultAndClickConfirm("× LEFT THUMB\n× LEFT INDEX FINGER\n× RIGHT THUMB\n× RIGHT INDEX FINGER\n")
        checkNoFingersScannedToastIsShown(getCurrentActivity() ?: failTest("No activity found"))
        checkFirstFingerYetToBeScanned()

        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)
        assertEquals(2, viewPager?.adapter?.count)
        assertEquals(0, viewPager?.currentItem)
    }

    @Test
    fun threeBadScanAndMaxNotReached_shouldAddAFinger() {
        mockScannerManagerWithScanner(ScannerWrapperV1(createMockedScannerV1 { queueBadFinger() }))

        scenario = ActivityScenario.launch(collectTaskRequest(TWO_FINGERS).toIntent())

        pressScan()
        pressScan()
        pressScan()

        waitForSplashScreenToAppearAndDisappear()

        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)
        assertEquals(3, viewPager?.adapter?.count)
        assertEquals(1, viewPager?.currentItem)
    }

    @Test
    fun threeBadScansAndMaxReached_shouldNotAddAFinger() {
        mockScannerManagerWithScanner(ScannerWrapperV1(createMockedScannerV1 { queueBadFinger() }))

        scenario = ActivityScenario.launch(collectTaskRequest(FOUR_FINGERS).toIntent())

        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)

        assertEquals(4, viewPager?.adapter?.count)

        pressScan()
        pressScan()
        pressScan()

        waitForSplashScreenToAppearAndDisappear()

        assertEquals(4, viewPager?.adapter?.count)
        assertEquals(1, viewPager?.currentItem)
    }

    @Test
    fun threeBadScansDueToMissingTemplates_shouldNotAddAFinger() {
        mockScannerManagerWithScanner(ScannerWrapperV1(createMockedScannerV1 { queueFingerNotDetected() }))

        scenario = ActivityScenario.launch(collectTaskRequest(TWO_FINGERS).toIntent())

        pressScan()
        pressScan()
        pressScan()

        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)
        assertEquals(2, viewPager?.adapter?.count)
        assertEquals(0, viewPager?.currentItem)
    }

    @Test
    fun skipFingerAndMaxNotReached_shouldAddAFinger() {
        mockScannerManagerWithScanner(ScannerWrapperV1(createMockedScannerV1()))

        scenario = ActivityScenario.launch(collectTaskRequest(TWO_FINGERS).toIntent())

        skipFinger()

        waitForSplashScreenToAppearAndDisappear()

        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)
        assertEquals(3, viewPager?.adapter?.count)
        assertEquals(1, viewPager?.currentItem)
    }

    @Test
    fun skipFingerAndMaxReached_shouldNotAddAFinger() {
        mockScannerManagerWithScanner(ScannerWrapperV1(createMockedScannerV1()))

        scenario = ActivityScenario.launch(collectTaskRequest(FOUR_FINGERS).toIntent())

        skipFinger()

        waitForSplashScreenToAppearAndDisappear()

        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)
        assertEquals(4, viewPager?.adapter?.count)
        assertEquals(1, viewPager?.currentItem)
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
        releaseFingerprintKoinModules()
    }

    companion object {
        private val TWO_FINGERS = listOf(
            FingerIdentifier.LEFT_THUMB,
            FingerIdentifier.LEFT_INDEX_FINGER
        )
        private val FOUR_FINGERS = listOf(
            FingerIdentifier.RIGHT_THUMB,
            FingerIdentifier.RIGHT_INDEX_FINGER,
            FingerIdentifier.LEFT_THUMB,
            FingerIdentifier.LEFT_INDEX_FINGER
        )

        private fun collectTaskRequest(fingersToCapture: List<FingerIdentifier>) =
            CollectFingerprintsTaskRequest(fingersToCapture)

        private fun CollectFingerprintsTaskRequest.toIntent() = Intent().also {
            it.setClassName(ApplicationProvider.getApplicationContext<Application>().packageName, CollectFingerprintsActivity::class.qualifiedName!!)
            it.putExtra(CollectFingerprintsTaskRequest.BUNDLE_KEY, this)
        }
    }
}
