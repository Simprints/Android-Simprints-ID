package com.simprints.id.activities.qrcapture

import android.Manifest.permission.CAMERA
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import com.simprints.id.Application
import com.simprints.id.activities.qrcapture.tools.CameraHelper
import com.simprints.id.activities.qrcapture.tools.QrCodeProducer
import com.simprints.id.activities.qrcapture.tools.QrPreviewBuilder
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.channels.Channel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class QrCaptureActivityAndroidTest {

    @get:Rule
    var grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(CAMERA)

    @MockK
    lateinit var mockCameraHelper: CameraHelper

    @MockK
    lateinit var mockQrPreviewBuilder: QrPreviewBuilder

    @MockK
    lateinit var mockQrCodeProducer: QrCodeProducer

    @MockK
    lateinit var mockChannel: Channel<String>

    private val app = ApplicationProvider.getApplicationContext<Application>()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun shouldStartCamera() {
        coEvery { mockChannel.receive() } returns VALID_QR_SCAN_RESULT

        qrCaptureActivity {
        } assert {
            cameraIsStarted()
        }
    }

    @Test
    fun whenQrCodeIsScanned_shouldReturnOkResult() {
        coEvery { mockChannel.receive() } returns VALID_QR_SCAN_RESULT

        qrCaptureActivity {
        } assert {
            resultIsOk()
        }
    }

    @Test
    fun whenQrCodeIsScanned_shouldSendValueInResult() {
        coEvery { mockChannel.receive() } returns VALID_QR_SCAN_RESULT

        qrCaptureActivity {
        } assert {
            qrScanResultIsSent()
        }
    }

    @Test
    fun whenQrCodeIsScanned_shouldFinishActivity() {
        coEvery { mockChannel.receive() } returns VALID_QR_SCAN_RESULT

        qrCaptureActivity {
        } assert {
            activityIsFinished()
        }
    }

    @Test
    fun cameraException_shouldSendCancelledResult() {
        coEvery { mockCameraHelper.startCamera(any(), any(), any()) } throws Exception()

        qrCaptureActivity {
        } assert {
            resultIsCancelled()
        }
    }

    @Test
    fun cameraException_shouldFinishActivity() {
        coEvery { mockCameraHelper.startCamera(any(), any(), any()) } throws Exception()

        qrCaptureActivity {
        } assert {
            activityIsFinished()
        }
    }

    @Test
    fun pressBack_shouldSendCancelledResult() {
        coEvery { mockChannel.receive() } returns INVALID_QR_SCAN_RESULT

        qrCaptureActivity {
        } pressBack {
            resultIsCancelled()
        }
    }

    @Test
    fun pressBack_shouldFinishActivity() {
        coEvery { mockChannel.receive() } returns INVALID_QR_SCAN_RESULT

        qrCaptureActivity {
        } pressBack {
            activityIsFinished()
        }
    }

}
