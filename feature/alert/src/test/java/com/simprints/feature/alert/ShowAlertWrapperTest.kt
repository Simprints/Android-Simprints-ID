package com.simprints.feature.alert

import android.content.Intent
import androidx.core.os.bundleOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.simprints.feature.alert.intent.AlertWrapperActivity
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShowAlertWrapperTest {

    private lateinit var wrapper: ShowAlertWrapper

    @Before
    fun setUp() {
        wrapper = ShowAlertWrapper()
    }


    @Test
    fun `createIntent passes provided bundle into intent extras`() {
        val intent = wrapper.createIntent(
            mockk { every { packageName } returns "" },
            bundleOf("test" to 42)
        )

        Truth.assertThat(intent.extras?.keySet()).isNotEmpty()
    }

    @Test
    fun `parseResult returns default result if no intent`() {
        val result = wrapper.parseResult(0, null)
        Truth.assertThat(result.buttonKey).isEmpty()
        Truth.assertThat(result.payload.keySet()).isEmpty()
    }

    @Test
    fun `parseResult returns default result if no data in intent`() {
        val result = wrapper.parseResult(0, Intent())
        Truth.assertThat(result.buttonKey).isEmpty()
        Truth.assertThat(result.payload.keySet()).isEmpty()
    }

    @Test
    fun `parseResult returns restores result if provided in intent`() {
        val intent = Intent().putExtra(
            AlertWrapperActivity.ALERT_RESULT,
            AlertResult(AlertContract.ALERT_BUTTON_PRESSED_BACK, bundleOf("test" to 42))
        )
        val result = wrapper.parseResult(0, intent)
        Truth.assertThat(result.isBackButtonPress()).isTrue()
        Truth.assertThat(result.payload.getInt("test")).isEqualTo(42)
    }
}
