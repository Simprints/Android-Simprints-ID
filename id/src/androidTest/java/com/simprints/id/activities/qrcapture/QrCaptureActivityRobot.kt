package com.simprints.id.activities.qrcapture

import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verifyBlocking
import com.simprints.id.activities.qrcapture.QrCaptureActivity.Companion.QR_RESULT_KEY
import com.simprints.id.activities.qrcapture.tools.CameraHelper

const val VALID_QR_SCAN_RESULT = "mock_qr_code"
const val INVALID_QR_SCAN_RESULT = ""

fun QrCaptureActivityAndroidTest.qrCaptureActivity(
    block: QrCaptureActivityRobot.() -> Unit
): QrCaptureActivityRobot {
    InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    val activityScenario = ActivityScenario.launch(QrCaptureActivity::class.java)

    return QrCaptureActivityRobot(activityScenario, mockCameraHelper).apply(block)
}

class QrCaptureActivityRobot(
    private val activityScenario: ActivityScenario<QrCaptureActivity>,
    private val mockCameraHelper: CameraHelper
) {

    infix fun pressBack(assertion: QrCaptureActivityAssertions.() -> Unit) {
        pressBackUnconditionally()

        assert(assertion)
    }

    infix fun assert(assertion: QrCaptureActivityAssertions.() -> Unit) {
        QrCaptureActivityAssertions(activityScenario, mockCameraHelper).run(assertion)
    }

}

class QrCaptureActivityAssertions(
    private val activityScenario: ActivityScenario<QrCaptureActivity>,
    private val mockCameraHelper: CameraHelper
) {

    fun cameraIsStarted() {
        verifyBlocking(mockCameraHelper) {
            startCamera(any(), any())
        }
    }

    fun resultIsOk() {
        assertThat(activityScenario.result.resultCode).isEqualTo(Activity.RESULT_OK)
    }

    fun resultIsCancelled() {
        assertThat(activityScenario.result.resultCode).isEqualTo(Activity.RESULT_CANCELED)
    }

    fun qrScanResultIsSent() {
        with(activityScenario.result.resultData) {
            assertThat(hasExtra(QR_RESULT_KEY)).isTrue()
            assertThat(getStringExtra(QR_RESULT_KEY)).isEqualTo(VALID_QR_SCAN_RESULT)
        }
    }

    fun activityIsFinished() {
        assertThat(activityScenario.state).isEqualTo(Lifecycle.State.DESTROYED)
    }

}
