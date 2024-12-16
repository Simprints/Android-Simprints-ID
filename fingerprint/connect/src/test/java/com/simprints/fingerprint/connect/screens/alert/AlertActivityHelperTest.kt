package com.simprints.fingerprint.connect.screens.alert

import android.app.Activity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.response.AppErrorReason
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
class AlertActivityHelperTest {
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
            result = AlertResult(AlertContract.ALERT_BUTTON_PRESSED_BACK),
            showRefusal = { refuseCalled = true },
            retry = {},
            finishWithError = {},
        )
        assertThat(refuseCalled).isTrue()
    }

    @Test
    fun finishes_whenHandlingBackFromUnexpectedError() {
        var finished = false
        helper.handleAlertResult(
            activity,
            result = AlertResult(AlertContract.ALERT_BUTTON_PRESSED_BACK, AppErrorReason.UNEXPECTED_ERROR),
            showRefusal = {},
            retry = {},
            finishWithError = { finished = true },
        )
        assertThat(finished).isTrue()
    }

    @Test
    fun triggersRetry_whenResumingAfterPairAction() {
        helper.handleResume { fail("Should not be called") }
        helper.handleAlertResult(
            mockk(relaxed = true),
            result = AlertResult(AlertError.ACTION_PAIR),
            showRefusal = {},
            retry = {},
            finishWithError = {},
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
            result = AlertResult(AlertError.ACTION_CLOSE),
            showRefusal = {},
            retry = {},
            finishWithError = {},
        )
        helper.handleResume { fail("Should not be called") }
    }

    @Test
    fun finishes_whenHandlingCloseAction() {
        var finished = false
        helper.handleAlertResult(
            activity,
            result = AlertResult(AlertError.ACTION_CLOSE),
            showRefusal = {},
            retry = {},
            finishWithError = { finished = true },
        )
        assertThat(finished).isTrue()
    }

    @Test
    fun opensRefusal_whenHandlingRefusalAction() {
        var refuseCalled = false
        helper.handleAlertResult(
            activity,
            result = AlertResult(AlertError.ACTION_REFUSAL),
            showRefusal = { refuseCalled = true },
            retry = {},
            finishWithError = {},
        )
        assertThat(refuseCalled).isTrue()
    }

    @Test
    fun triggersRetry_whenHandlingRetryAction() {
        var retryCalled = false
        helper.handleAlertResult(
            activity,
            result = AlertResult(AlertError.ACTION_RETRY),
            showRefusal = {},
            retry = { retryCalled = true },
            finishWithError = {},
        )
        assertThat(retryCalled).isTrue()
    }

    @Test
    fun opensSettings_whenHandlingSettingsAction() {
        helper.handleAlertResult(
            activity,
            result = AlertResult(AlertError.ACTION_BT_SETTINGS),
            showRefusal = {},
            retry = {},
            finishWithError = {},
        )
        verify { activity.startActivity(any()) }
    }

    @Test
    fun opensSettings_whenOpensAppSettings() {
        helper.handleAlertResult(
            activity,
            result = AlertResult(AlertError.ACTION_APP_SETTINGS),
            showRefusal = {},
            retry = {},
            finishWithError = {},
        )
        verify { activity.startActivity(any()) }
    }
}
