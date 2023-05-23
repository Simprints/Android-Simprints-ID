package com.simprints.fingerprint.activities.alert

import android.app.Activity
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.AlertResult
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
        var refuseCalled = false
        helper.handleAlertResult(
            activity,
            result = AlertResult(
                AlertContract.ALERT_BUTTON_PRESSED_BACK,
                bundleOf(AlertError.PAYLOAD_KEY to AlertError.LOW_BATTERY.name)
            ),
            showRefusal = { refuseCalled = true },
            retry = {}
        )
        assertThat(refuseCalled).isTrue()
        verify(exactly = 0) { activity.finish() }
    }

    @Test
    fun finishes_whenHandlingBackFromUnexpectedError() {
        helper.handleAlertResult(
            activity,
            result = AlertResult(
                AlertContract.ALERT_BUTTON_PRESSED_BACK,
                bundleOf(AlertError.PAYLOAD_KEY to AlertError.UNEXPECTED_ERROR.name)
            ),
            showRefusal = {},
            retry = {}
        )
        verify { activity.finish() }
    }

    @Test
    fun finishes_whenHandlingBackFromMalformedError() {
        helper.handleAlertResult(
            activity,
            result = AlertResult(AlertContract.ALERT_BUTTON_PRESSED_BACK, Bundle()),
            showRefusal = {},
            retry = {}
        )
        verify { activity.finish() }
    }

    @Test
    fun triggersRetry_whenResumingAfterPairAction() {
        helper.handleResume { fail("Should not be called") }

        helper.handleAlertResult(
            mockk(relaxed = true),
            result = AlertResult(AlertError.ACTION_PAIR, Bundle()),
            showRefusal = {},
            retry = {}
        )

        var resumeCalled = false
        helper.handleResume { resumeCalled = true }
        assertThat(resumeCalled).isTrue()
    }

    @Test
    fun doesNotTriggersRetry_whenResumingNotAfterPairAction() {
        helper.handleResume { fail("Should not be called") }

        helper.handleAlertResult(
            mockk(relaxed = true),
            result = AlertResult(AlertError.ACTION_CLOSE, Bundle()),
            showRefusal = {},
            retry = {}
        )

        helper.handleResume { fail("Should not be called") }
    }

    @Test
    fun finishes_whenHandlingCloseAction() {
        helper.handleAlertResult(
            activity,
            result = AlertResult(AlertError.ACTION_CLOSE, Bundle()),
            showRefusal = {},
            retry = {}
        )
        verify { activity.finish() }
    }

    @Test
    fun opensRefusal_whenHandlingRefusalAction() {
        var refuseCalled = false
        helper.handleAlertResult(
            activity,
            result = AlertResult(AlertError.ACTION_REFUSAL, Bundle()),
            showRefusal = { refuseCalled = true },
            retry = {}
        )
        assertThat(refuseCalled).isTrue()
        verify(exactly = 0) { activity.finish() }
    }

    @Test
    fun triggersRetry_whenHandlingRetryAction() {
        var retryCalled = false
        helper.handleAlertResult(
            activity,
            result = AlertResult(AlertError.ACTION_RETRY, Bundle()),
            showRefusal = {},
            retry = { retryCalled = true }
        )

        assertThat(retryCalled).isTrue()
        verify(exactly = 0) { activity.finish() }
    }

    @Test
    fun opensSettings_whenHandlingSettingsAction() {
        helper.handleAlertResult(
            activity,
            result = AlertResult(AlertError.ACTION_BT_SETTINGS, Bundle()),
            showRefusal = {},
            retry = {}
        )
        verify { activity.startActivity(any()) }
        verify(exactly = 0) { activity.finish() }
    }
}
