package com.simprints.id.activities.qrcapture

import android.Manifest.permission.CAMERA
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.simprints.id.activities.qrcapture.tools.CameraBinder
import com.simprints.id.activities.qrcapture.tools.QrCodeProducer
import com.simprints.id.activities.qrcapture.tools.QrPreviewBuilder
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.di.DependencyRule
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
@ExperimentalCoroutinesApi
class QrCaptureActivityTest {

    @get:Rule var grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(CAMERA)

    @MockK lateinit var mockCameraBinder: CameraBinder
    @MockK lateinit var mockQrPreviewBuilder: QrPreviewBuilder
    @MockK lateinit var mockQrCodeProducer: QrCodeProducer

    private val appModule by lazy {
        val app = ApplicationProvider.getApplicationContext<TestApplication>()

        TestAppModule(
            app,
            cameraBinderRule = DependencyRule.ReplaceRule {
                mockCameraBinder
            },
            qrPreviewBuilderRule = DependencyRule.ReplaceRule {
                mockQrPreviewBuilder
            },
            qrCodeProducerRule = DependencyRule.ReplaceRule {
                mockQrCodeProducer
            }
        )
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        UnitTestConfig(this, appModule).initAndInjectComponent()
    }

    @Test
    fun shouldStartCamera() {
        qrCaptureActivity {
        } assert {
            cameraIsStarted()
        }
    }

    @Test
    fun whenQrCodeIsScanned_shouldReturnOkResult() {
        qrCaptureActivity {
        } scanQrCode {
            resultIsOk()
        }
    }

    @Test
    fun whenQrCodeIsScanned_shouldSendValueInResult() {
        qrCaptureActivity {
        } scanQrCode {
            qrScanResultIsSent()
        }
    }

    @Test
    fun pressBack_shouldSendCancelledResult() {
        qrCaptureActivity {
        } pressBack {
            resultIsCancelled()
        }
    }

    @Test
    fun whenQrCodeIsScanned_shouldFinishActivity() {
        qrCaptureActivity {
        } scanQrCode {
            activityIsFinished()
        }
    }

    @Test
    fun pressBack_shouldFinishActivity() {
        qrCaptureActivity {
        } pressBack {
            activityIsFinished()
        }
    }

}
