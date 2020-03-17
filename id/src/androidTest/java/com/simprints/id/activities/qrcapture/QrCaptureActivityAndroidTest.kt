package com.simprints.id.activities.qrcapture

import android.Manifest.permission.CAMERA
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import com.simprints.id.Application
import com.simprints.id.activities.qrcapture.tools.CameraBinder
import com.simprints.id.activities.qrcapture.tools.QrCodeProducer
import com.simprints.id.activities.qrcapture.tools.QrPreviewBuilder
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.mock
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.channels.Channel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class QrCaptureActivityAndroidTest {

    @get:Rule var grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(CAMERA)

    var mockCameraBinder: CameraBinder = mock()

    @MockK lateinit var mockQrPreviewBuilder: QrPreviewBuilder
    @MockK lateinit var mockQrCodeProducer: QrCodeProducer
    @MockK lateinit var mockChannel: Channel<String>

    private val app = ApplicationProvider.getApplicationContext<Application>()

    private val appModule by lazy {
        TestAppModule(
            app,
            cameraBinderRule = DependencyRule.ReplaceRule {
                mockCameraBinder
            },
            qrPreviewBuilderRule = DependencyRule.ReplaceRule {
                mockQrPreviewBuilder
            },
            qrCodeProducerRule = DependencyRule.ReplaceRule {
                mockQrCodeProducer.apply {
                    coEvery { qrCodeChannel } returns mockChannel
                }
            }
        )
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        AndroidTestConfig(this, appModule).initAndInjectComponent()
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
