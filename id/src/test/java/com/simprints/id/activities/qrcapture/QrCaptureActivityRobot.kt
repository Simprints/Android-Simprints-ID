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
fun QrCaptureActivityTest.qrCaptureActivity(
    block: QrCaptureActivityRobot.() -> Unit
): QrCaptureActivityRobot {
    val activityScenario = ActivityScenario.launch(QrCaptureActivity::class.java)

    return QrCaptureActivityRobot(
        activityScenario,
        mockCameraBinder,
        mockQrCodeProducer
    ).apply(block)
}

class QrCaptureActivityRobot(
    private val activityScenario: ActivityScenario<QrCaptureActivity>,
    private val mockCameraBinder: CameraBinder,
    private val mockQrCodeProducer: QrCodeProducer
) {

    infix fun scanQrCode(assertion: QrCaptureActivityAssertions.() -> Unit) {
        coEvery { mockQrCodeProducer.qrCodeChannel.receive() } returns QR_SCAN_RESULT

        assert(assertion)
    }

    infix fun pressBack(assertion: QrCaptureActivityAssertions.() -> Unit) {
        pressBackUnconditionally()

        assert(assertion)
    }

    infix fun assert(assertion: QrCaptureActivityAssertions.() -> Unit) {
        QrCaptureActivityAssertions(activityScenario, mockCameraBinder).run(assertion)
    }

}

class QrCaptureActivityAssertions(
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
