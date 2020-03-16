package com.simprints.id.activities.qrcapture

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBackUnconditionally
import com.google.common.truth.Truth.assertThat
import com.simprints.id.activities.qrcapture.QrCaptureActivity.Companion.QR_RESULT_KEY
import com.simprints.id.activities.qrcapture.tools.CameraBinder
import com.simprints.id.activities.qrcapture.tools.QrCodeProducer
import io.mockk.coEvery
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi

private const val QR_SCAN_RESULT = "mock_qr_code"

@ExperimentalCoroutinesApi
fun QrCaptureActivityAndroidTest.qrCaptureActivity(
    block: QrCaptureActivityAndroidTestRobot.() -> Unit
): QrCaptureActivityAndroidTestRobot {
    val activityScenario = ActivityScenario.launch(QrCaptureActivity::class.java)

    return QrCaptureActivityAndroidTestRobot(
        activityScenario,
        mockCameraBinder,
        mockQrCodeProducer
    ).apply(block)
}

class QrCaptureActivityAndroidTestRobot(
    private val activityScenario: ActivityScenario<QrCaptureActivity>,
    private val mockCameraBinder: CameraBinder,
    private val mockQrCodeProducer: QrCodeProducer
) {

    infix fun scanQrCode(assertion: QrCaptureActivityAndroidTestAssertions.() -> Unit) {
        coEvery { mockQrCodeProducer.qrCodeChannel.receive() } returns QR_SCAN_RESULT

        assert(assertion)
    }

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
        verify { mockCameraBinder.bindToLifecycle(any(), any(), any()) }
    }

    fun resultIsOk() {
        assertThat(activityScenario.result.resultCode).isEqualTo(Activity.RESULT_OK)
    }

    fun resultIsCancelled() {
        assertThat(activityScenario.result.resultCode).isEqualTo(Activity.RESULT_CANCELED)
    }

    fun qrScanResultIsSent() {
        val expectedResultData = Intent().putExtra(QR_RESULT_KEY, QR_SCAN_RESULT)
        assertThat(activityScenario.result.resultData).isEqualTo(expectedResultData)
    }

    fun activityIsFinished() {
        assertThat(activityScenario.state).isEqualTo(Lifecycle.State.DESTROYED)
    }

}
