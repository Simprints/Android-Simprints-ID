package com.simprints.feature.alert

import androidx.core.os.bundleOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.response.AppErrorReason
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertResultTest {

    @Test
    fun `app reason default to unexpected if not provide`() {
        val alertResult = AlertResult("buttonKey", bundleOf())
        assertThat(alertResult.appErrorReason()).isEqualTo(AppErrorReason.UNEXPECTED_ERROR)
    }

    @Test
    fun `app reason default to unexpected if invalid`() {
        val alertResult =
            AlertResult("buttonKey", bundleOf(AlertContract.ALERT_REASON_PAYLOAD to "invalid"))
        assertThat(alertResult.appErrorReason()).isEqualTo(AppErrorReason.UNEXPECTED_ERROR)
    }

    @Test
    fun `app reason returns correctly`() {
        val alertResult = AlertResult(
            "buttonKey",
            bundleOf(AlertContract.ALERT_REASON_PAYLOAD to AppErrorReason.FACE_LICENSE_INVALID.name)
        )
        assertThat(alertResult.appErrorReason()).isEqualTo(AppErrorReason.FACE_LICENSE_INVALID)
    }
}
