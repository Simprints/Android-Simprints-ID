package com.simprints.fingerprint.activities.collectfingerprint

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.collect.CollectFingerprintsActivity
import com.simprints.fingerprint.activities.collect.ViewPagerCustom
import com.simprints.fingerprint.activities.collect.models.FingerIdentifier
import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsTaskRequest
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.commontesttools.di.TestFingerprintModule
import com.simprints.fingerprint.controllers.scanner.ScannerManager
import com.simprints.fingerprint.data.domain.Action
import com.simprints.fingerprint.testtools.AndroidTestConfig
import com.simprints.fingerprint.testtools.scanner.*
import com.simprints.testtools.android.getCurrentActivity
import com.simprints.testtools.common.di.DependencyRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class CollectFingerprintsActivityTest {

    @get:Rule val collectFingerprintsRule = ActivityTestRule(CollectFingerprintsActivity::class.java, false, false)

    @Inject lateinit var scannerManagerMock: ScannerManager

    private val fingerprintModule by lazy {
        TestFingerprintModule(
            scannerManagerRule = DependencyRule.ReplaceRule { scannerManagerMock })
    }

    @Test
    fun twoGoodScansAndThenConfirm_finishesWithCorrectResult() {
        scannerManagerMock = ScannerManagerMock(createMockedScanner())

        AndroidTestConfig(this, fingerprintModule).fullSetup()

        collectFingerprintsRule.launchActivity(collectTaskRequest(FINGER_STATUS_TWO_FINGERS).toIntent())

        pressScan()
        pressScan()
        checkIfDialogIsDisplayedWithResultAndClickConfirm()

        val result = collectFingerprintsRule.activityResult.resultData.extras
            ?.getParcelable<CollectFingerprintsTaskResult>(CollectFingerprintsTaskResult.BUNDLE_KEY)

        assertNotNull(result)
        assertEquals(2, result?.probe?.fingerprints?.size)
    }

    @Test
    fun mixedScansAndThenConfirm_finishesWithCorrectResult() {
        val scanner = createMockedScanner()
        scannerManagerMock = ScannerManagerMock(scanner)

        AndroidTestConfig(this, fingerprintModule).fullSetup()

        collectFingerprintsRule.launchActivity(collectTaskRequest(FINGER_STATUS_TWO_FINGERS).toIntent())

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

        val result = collectFingerprintsRule.activityResult.resultData.extras
            ?.getParcelable<CollectFingerprintsTaskResult>(CollectFingerprintsTaskResult.BUNDLE_KEY)

        assertNotNull(result)
        result?.probe?.fingerprints?.let {
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
    fun threeBadScanAndMaxNotReached_shouldAddAFinger() {
        scannerManagerMock = ScannerManagerMock(createMockedScanner { queueBadFinger() })

        AndroidTestConfig(this, fingerprintModule).fullSetup()

        collectFingerprintsRule.launchActivity(collectTaskRequest(FINGER_STATUS_TWO_FINGERS).toIntent())

        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)

        pressScan()
        pressScan()
        pressScan()

        waitForSplashScreenToAppearAndDisappear()

        assertEquals(3, viewPager?.adapter?.count)
        assertEquals(1, viewPager?.currentItem)
    }

    @Test
    fun threeBadAndMaxReached_shouldNotAddAFinger() {
        scannerManagerMock = ScannerManagerMock(createMockedScanner { queueBadFinger() })

        AndroidTestConfig(this, fingerprintModule).fullSetup()

        collectFingerprintsRule.launchActivity(collectTaskRequest(FINGER_STATUS_FOUR_FINGERS).toIntent())

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
        scannerManagerMock = ScannerManagerMock(createMockedScanner { queueFingerNotDetected() })

        AndroidTestConfig(this, fingerprintModule).fullSetup()

        collectFingerprintsRule.launchActivity(collectTaskRequest(FINGER_STATUS_TWO_FINGERS).toIntent())

        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)

        pressScan()
        pressScan()
        pressScan()

        assertEquals(2, viewPager?.adapter?.count)
        assertEquals(0, viewPager?.currentItem)
    }

    @Test
    fun skipFingerAndMaxNotReached_shouldAddAFinger() {
        scannerManagerMock = ScannerManagerMock(createMockedScanner())

        AndroidTestConfig(this, fingerprintModule).fullSetup()

        collectFingerprintsRule.launchActivity(collectTaskRequest(FINGER_STATUS_TWO_FINGERS).toIntent())

        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)

        skipFinger()

        waitForSplashScreenToAppearAndDisappear()

        assertEquals(3, viewPager?.adapter?.count)
        assertEquals(1, viewPager?.currentItem)
    }

    @Test
    fun skipFingerAndMaxReached_shouldNotAddAFinger() {
        scannerManagerMock = ScannerManagerMock(createMockedScanner())

        AndroidTestConfig(this, fingerprintModule).fullSetup()

        collectFingerprintsRule.launchActivity(collectTaskRequest(FINGER_STATUS_FOUR_FINGERS).toIntent())

        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)

        skipFinger()

        waitForSplashScreenToAppearAndDisappear()

        assertEquals(4, viewPager?.adapter?.count)
        assertEquals(1, viewPager?.currentItem)
    }

    companion object {
        private const val DEFAULT_PROJECT_ID = "some_project_id"
        private const val DEFAULT_USER_ID = "some_user_id"
        private const val DEFAULT_MODULE_ID = "some_module_id"
        private const val DEFAULT_LANGUAGE = "en"
        private val DEFAULT_ACTION = Action.ENROL
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

        private fun collectTaskRequest(fingerStatus: Map<FingerIdentifier, Boolean>) =
            CollectFingerprintsTaskRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID,
                DEFAULT_ACTION, DEFAULT_LANGUAGE, fingerStatus)

        private fun CollectFingerprintsTaskRequest.toIntent() = Intent().also {
            it.putExtra(CollectFingerprintsTaskRequest.BUNDLE_KEY, this)
        }
    }
}
