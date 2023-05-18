package com.simprints.feature.exitform

import android.content.Intent
import androidx.core.os.bundleOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.simprints.feature.exitform.config.ExitFormOption
import com.simprints.feature.exitform.screen.ExitFormWrapperActivity
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before

import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShowExitFormWrapperTest {

    private lateinit var wrapper: ShowExitFormWrapper

    @Before
    fun setUp() {
        wrapper = ShowExitFormWrapper()
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
        Truth.assertThat(result.wasSubmitted).isFalse()
    }

    @Test
    fun `parseResult returns default result if no data in intent`() {
        val result = wrapper.parseResult(0, Intent())
        Truth.assertThat(result.wasSubmitted).isFalse()
    }


    @Test
    fun `parseResult returns restores result if provided in intent`() {
        val intent = Intent().putExtra(
            ExitFormWrapperActivity.EXIT_FORM_RESULT,
            ExitFormResult(true, ExitFormOption.NoPermission)
        )
        val result = wrapper.parseResult(0, intent)
        Truth.assertThat(result.wasSubmitted).isTrue()
        Truth.assertThat(result.selectedOption).isEqualTo(ExitFormOption.NoPermission)
    }
}
