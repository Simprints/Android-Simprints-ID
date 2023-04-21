package com.simprints.fingerprint.activities.alert

import android.app.Activity
import androidx.core.os.bundleOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.alert.AlertContract
import com.simprints.fingerprint.orchestrator.domain.RequestCode
import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class AlertActivityHelperTest {

    private val activity = mockk<Activity>(relaxed = true)

    private lateinit var helper: AlertActivityHelper

    @Before
    fun setUp() {
        clearMocks(activity)

        helper = AlertActivityHelper()
    }

    @Test
    fun opensRefusal_whenHandlingBackFromExpectedError() {
        helper.handleAlertResult(
            activity,
            bundleOf(
                AlertContract.ALERT_BUTTON_PRESSED to AlertContract.ALERT_BUTTON_PRESSED_BACK,
                AlertContract.ALERT_PAYLOAD to bundleOf(
                    AlertError.PAYLOAD_KEY to AlertError.LOW_BATTERY.name,
                ),
            )
        ) {}
        verify { activity.startActivityForResult(any(), RequestCode.REFUSAL.value) }
        verify(exactly = 0) { activity.finish() }
    }

    @Test
    fun finishes_whenHandlingBackFromUnexpectedError() {
        helper.handleAlertResult(
            activity,
            bundleOf(
                AlertContract.ALERT_BUTTON_PRESSED to AlertContract.ALERT_BUTTON_PRESSED_BACK,
                AlertContract.ALERT_PAYLOAD to bundleOf(
                    AlertError.PAYLOAD_KEY to AlertError.UNEXPECTED_ERROR.name,
                ),
            )
        ) {}
        verify { activity.finish() }
    }

    @Test
    fun finishes_whenHandlingBackFromMalformedError() {
        helper.handleAlertResult(
            activity,
            bundleOf(AlertContract.ALERT_BUTTON_PRESSED to AlertContract.ALERT_BUTTON_PRESSED_BACK)
        ) {}
        verify { activity.finish() }
    }

    @Test
    fun triggersRetry_whenResumingAfterPairAction() {
        helper.handleResume { fail("Should not be called") }

        helper.handleAlertResult(
            mockk(relaxed = true),
            bundleOf(AlertContract.ALERT_BUTTON_PRESSED to AlertError.ACTION_PAIR)
        ) {}

        var resumeCalled = false
        helper.handleResume { resumeCalled = true }
        assertThat(resumeCalled).isTrue()
    }

    @Test
    fun doesNotTriggersRetry_whenResumingNotAfterPairAction() {
        helper.handleResume { fail("Should not be called") }

        helper.handleAlertResult(
            mockk(relaxed = true),
            bundleOf(AlertContract.ALERT_BUTTON_PRESSED to AlertError.ACTION_CLOSE)
        ) {}

        helper.handleResume { fail("Should not be called") }
    }

    @Test
    fun finishes_whenHandlingCloseAction() {
        helper.handleAlertResult(
            activity,
            bundleOf(AlertContract.ALERT_BUTTON_PRESSED to AlertError.ACTION_CLOSE)
        ) {}
        verify { activity.finish() }
    }

    @Test
    fun opensRefusal_whenHandlingRefusalAction() {
        helper.handleAlertResult(
            activity,
            bundleOf(AlertContract.ALERT_BUTTON_PRESSED to AlertError.ACTION_REFUSAL)
        ) {}
        verify { activity.startActivityForResult(any(), RequestCode.REFUSAL.value) }
        verify(exactly = 0) { activity.finish() }
    }

    @Test
    fun triggersRetry_whenHandlingRetryAction() {
        var retryCalled = false
        helper.handleAlertResult(
            activity,
            bundleOf(AlertContract.ALERT_BUTTON_PRESSED to AlertError.ACTION_RETRY)
        ) { retryCalled = true }

        assertThat(retryCalled).isTrue()
        verify(exactly = 0) { activity.finish() }
    }

    @Test
    fun opensSettings_whenHandlingSettingsAction() {
        helper.handleAlertResult(
            activity,
            bundleOf(AlertContract.ALERT_BUTTON_PRESSED to AlertError.ACTION_BT_SETTINGS)
        ) {}
        verify { activity.startActivity(any()) }
        verify(exactly = 0) { activity.finish() }
    }
}
