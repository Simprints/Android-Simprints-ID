package com.simprints.fingerprint.activities.refusal

import androidx.core.os.bundleOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.exitform.ExitFormContract
import com.simprints.feature.exitform.config.ExitFormOption
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RefusalAlertHelperTest {

    private val helper = RefusalAlertHelper

    @Test
    fun `handleResult calls onBack if data is empty`() {
        var backCalled = false

        helper.handleRefusal(
            bundleOf(),
            onBack = { backCalled = true },
            onSubmit = { fail("Should not call submit") }
        )
        assertThat(backCalled).isTrue()
    }

    @Test
    fun `handleResult calls onBack if form was not submitted`() {
        var backCalled = false

        helper.handleRefusal(
            bundleOf(ExitFormContract.EXIT_FORM_SUBMITTED to false),
            onBack = { backCalled = true },
            onSubmit = { fail("Should not call submit") }
        )
        assertThat(backCalled).isTrue()
    }

    @Test
    fun `handleResult calls onBack if form was submitted without option`() {
        var backCalled = false

        helper.handleRefusal(
            bundleOf(ExitFormContract.EXIT_FORM_SUBMITTED to true),
            onBack = { backCalled = true },
            onSubmit = { fail("Should not call submit") }
        )
        assertThat(backCalled).isTrue()
    }

    @Test
    fun `handleResult calls onSubmit if form was submitted with option`() {
        var submitCalled = false

        helper.handleRefusal(
            bundleOf(
                ExitFormContract.EXIT_FORM_SUBMITTED to true,
                ExitFormContract.EXIT_FORM_SELECTED_OPTION to ExitFormOption.Other,
            ),
            onBack = { fail("Should not call back") },
            onSubmit = { submitCalled = true }
        )
        assertThat(submitCalled).isTrue()
    }
}
