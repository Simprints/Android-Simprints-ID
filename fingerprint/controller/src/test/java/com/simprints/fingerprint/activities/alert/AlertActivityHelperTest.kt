package com.simprints.fingerprint.activities.alert

import android.app.Activity
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.test.ext.junit.runners.AndroidJUnit4
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
    fun finishes_whenHandlingBackFromUnexpectedError() {
        helper.handleAlertResult(
            activity,
            result = AlertResult(
                AlertContract.ALERT_BUTTON_PRESSED_BACK,
                bundleOf(AlertError.PAYLOAD_KEY to AlertError.UNEXPECTED_ERROR.name)
            ),
            showRefusal = {},
        )
        verify { activity.finish() }
    }

    @Test
    fun finishes_whenHandlingBackFromMalformedError() {
        helper.handleAlertResult(
            activity,
            result = AlertResult(AlertContract.ALERT_BUTTON_PRESSED_BACK, Bundle()),
            showRefusal = {},
        )
        verify { activity.finish() }
    }

    @Test
    fun doesNotTriggersRetry_whenResumingNotAfterPairAction() {
        helper.handleResume { fail("Should not be called") }

        helper.handleAlertResult(
            mockk(relaxed = true),
            result = AlertResult(AlertError.ACTION_CLOSE, Bundle()),
            showRefusal = {},
        )

        helper.handleResume { fail("Should not be called") }
    }

    @Test
    fun finishes_whenHandlingCloseAction() {
        helper.handleAlertResult(
            activity,
            result = AlertResult(AlertError.ACTION_CLOSE, Bundle()),
            showRefusal = {},
        )
        verify { activity.finish() }
    }
}
