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
import com.simprints.fingerprint.testtools.collectFingerprintsPressScan
import com.simprints.fingerprint.testtools.scanner.ScannerManagerMock
import com.simprints.fingerprint.testtools.scanner.createMockedScanner
import com.simprints.fingerprint.testtools.scanner.makeCallbackFailing
import com.simprints.fingerprint.testtools.skipFinger
import com.simprints.fingerprint.testtools.waitForSplashScreenAppearsAndDisappears
import com.simprints.fingerprintscanner.SCANNER_ERROR
import com.simprints.testtools.android.getCurrentActivity
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.anyOrNull
import com.simprints.testtools.common.syntax.wheneverThis
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import javax.inject.Inject

@MediumTest
@RunWith(AndroidJUnit4::class)
class CollectFingerprintsActivityTest {

    @get:Rule val collectFingerprintsRule = ActivityTestRule(CollectFingerprintsActivity::class.java, false, false)

    @Inject lateinit var scannerManagerMock: ScannerManager

    private val fingerprintModule by lazy {
        TestFingerprintModule(
            scannerManagerRule = DependencyRule.ReplaceRule { scannerManagerMock })
    }

    @Test
    fun threeBadScanAndMaxNotReached_shouldAddAFinger() {
        scannerManagerMock = ScannerManagerMock(createMockedScanner {
            wheneverThis { imageQuality } thenReturn 20 // Bad scan
        })

        AndroidTestConfig(this, fingerprintModule).fullSetup()

        collectFingerprintsRule.launchActivity(collectTaskRequest(FINGER_STATUS_TWO_FINGERS).toIntent())

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
        scannerManagerMock = ScannerManagerMock(createMockedScanner {
            wheneverThis { imageQuality } thenReturn 20 // Bad scan
        })

        AndroidTestConfig(this, fingerprintModule).fullSetup()

        collectFingerprintsRule.launchActivity(collectTaskRequest(FINGER_STATUS_FOUR_FINGERS).toIntent())

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
        scannerManagerMock = ScannerManagerMock(createMockedScanner {
            // SCANNER_ERROR.UN20_SDK_ERROR corresponds to no finger detected on sensor
            makeCallbackFailing(SCANNER_ERROR.UN20_SDK_ERROR) { startContinuousCapture(anyInt(), anyLong(), anyOrNull()) }
            makeCallbackFailing(SCANNER_ERROR.UN20_SDK_ERROR) { forceCapture(anyInt(), anyOrNull()) }
        })

        AndroidTestConfig(this, fingerprintModule).fullSetup()

        collectFingerprintsRule.launchActivity(collectTaskRequest(FINGER_STATUS_TWO_FINGERS).toIntent())

        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)

        collectFingerprintsPressScan()
        collectFingerprintsPressScan()
        collectFingerprintsPressScan()

        assertEquals(2, viewPager?.adapter?.count)
        assertEquals(0, viewPager?.currentItem)
    }

    @Test
    fun skipFingerAndMaxNotReached_shouldAddAFinger() {
        scannerManagerMock = ScannerManagerMock(createMockedScanner {
            // SCANNER_ERROR.UN20_SDK_ERROR corresponds to no finger detected on sensor
            makeCallbackFailing(SCANNER_ERROR.UN20_SDK_ERROR) { startContinuousCapture(anyInt(), anyLong(), anyOrNull()) }
            makeCallbackFailing(SCANNER_ERROR.UN20_SDK_ERROR) { forceCapture(anyInt(), anyOrNull()) }
        })

        AndroidTestConfig(this, fingerprintModule).fullSetup()

        collectFingerprintsRule.launchActivity(collectTaskRequest(FINGER_STATUS_TWO_FINGERS).toIntent())

        val viewPager = getCurrentActivity()?.findViewById<ViewPagerCustom>(R.id.view_pager)

        skipFinger()

        waitForSplashScreenAppearsAndDisappears()

        assertEquals(3, viewPager?.adapter?.count)
        assertEquals(1, viewPager?.currentItem)
    }

    @Test
    fun skipFingerAndMaxReached_shouldNotAddAFinger() {
        scannerManagerMock = ScannerManagerMock(createMockedScanner {
            // SCANNER_ERROR.UN20_SDK_ERROR corresponds to no finger detected on sensor
            makeCallbackFailing(SCANNER_ERROR.UN20_SDK_ERROR) { startContinuousCapture(anyInt(), anyLong(), anyOrNull()) }
            makeCallbackFailing(SCANNER_ERROR.UN20_SDK_ERROR) { forceCapture(anyInt(), anyOrNull()) }
        })

        AndroidTestConfig(this, fingerprintModule).fullSetup()

        collectFingerprintsRule.launchActivity(collectTaskRequest(FINGER_STATUS_FOUR_FINGERS).toIntent())

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
