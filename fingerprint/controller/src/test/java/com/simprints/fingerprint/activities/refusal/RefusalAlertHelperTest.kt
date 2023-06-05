package com.simprints.fingerprint.activities.refusal

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.exitform.config.ExitFormConfiguration
import com.simprints.feature.exitform.config.ExitFormOption
import com.simprints.feature.exitform.scannerOptions
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RefusalAlertHelperTest {

    private val helper = RefusalAlertHelper

    @Test
    fun `default refusal args include scanner options`() {
        val args = helper.refusalArgs()

        // there must be only 1 item args bundle
        val configKey = args.keySet().first()
        assertThat(args.getParcelable<ExitFormConfiguration>(configKey)?.visibleOptions)
            .containsExactlyElementsIn(scannerOptions())
    }

    @Test
    fun `handleRefusal calls onBack if data is empty`() {
        var backCalled = false

        helper.handleRefusal(
            ExitFormResult(false),
            onBack = { backCalled = true },
            onSubmit = { fail("Should not call submit") }
        )
        assertThat(backCalled).isTrue()
    }

    @Test
    fun `handleRefusal calls onBack if form was not submitted`() {
        var backCalled = false

        helper.handleRefusal(
            ExitFormResult(false),
            onBack = { backCalled = true },
            onSubmit = { fail("Should not call submit") }
        )
        assertThat(backCalled).isTrue()
    }

    @Test
    fun `handleRefusal calls onBack if form was submitted without option`() {
        var backCalled = false

        helper.handleRefusal(
            ExitFormResult(true),
            onBack = { backCalled = true },
            onSubmit = { fail("Should not call submit") }
        )
        assertThat(backCalled).isTrue()
    }

    @Test
    fun `handleRefusal calls onSubmit if form was submitted with option`() {
        var submitCalled = false

        helper.handleRefusal(
            ExitFormResult(true, ExitFormOption.Other),
            onBack = { fail("Should not call back") },
            onSubmit = { submitCalled = true }
        )
        assertThat(submitCalled).isTrue()
    }

    @Test
    fun `handleRefusal calls onSubmit if form was submitted with option and reason`() {
        var submitCalled = false

        helper.handleRefusal(
            ExitFormResult(true, ExitFormOption.Other, "reason"),
            onBack = { fail("Should not call back") },
            onSubmit = { submitCalled = true }
        )
        assertThat(submitCalled).isTrue()
    }
}
