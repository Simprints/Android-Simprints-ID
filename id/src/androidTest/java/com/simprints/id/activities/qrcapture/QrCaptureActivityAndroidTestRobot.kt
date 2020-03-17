package com.simprints.id.activities.qrcapture

import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.simprints.id.activities.qrcapture.QrCaptureActivity.Companion.QR_RESULT_KEY
import com.simprints.id.activities.qrcapture.tools.CameraBinder

private const val QR_SCAN_RESULT = "mock_qr_code"

fun QrCaptureActivityAndroidTest.qrCaptureActivity(
    block: QrCaptureActivityAndroidTestRobot.() -> Unit
): QrCaptureActivityAndroidTestRobot {
    InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    val activityScenario = ActivityScenario.launch(QrCaptureActivity::class.java)

    return QrCaptureActivityAndroidTestRobot(activityScenario, mockCameraBinder).apply(block)
}

class QrCaptureActivityAndroidTestRobot(
    private val activityScenario: ActivityScenario<QrCaptureActivity>,
    private val mockCameraBinder: CameraBinder
) {

    infix fun pressBack(assertion: QrCaptureActivityAndroidTestAssertions.() -> Unit) {
        pressBackUnconditionally()

        assert(assertion)
    }

    infix fun assert(assertion: QrCaptureActivityAndroidTestAssertions.() -> Unit) {
        QrCaptureActivityAndroidTestAssertions(activityScenario, mockCameraBinder).run(assertion)
    }

}

class QrCaptureActivityAndroidTestAssertions(
    private val activityScenario: ActivityScenario<QrCaptureActivity>,
    private val mockCameraBinder: CameraBinder
) {

    fun cameraIsStarted() {
        verify(mockCameraBinder).bindToLifecycle(any(), any(), any())
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
            assertThat(getStringExtra(QR_RESULT_KEY)).isEqualTo(QR_SCAN_RESULT)
        }
    }

    fun activityIsFinished() {
        assertThat(activityScenario.state).isEqualTo(Lifecycle.State.DESTROYED)
    }

}
