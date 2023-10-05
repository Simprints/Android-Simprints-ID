package com.simprints.id.exitformhandler

import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.exitform.config.ExitFormOption
import com.simprints.feature.exitform.screen.ExitFormFragmentArgs
import com.simprints.id.orchestrator.steps.core.response.ExitFormResponse
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.resources.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class ExitFormHelperImplTest {

    private lateinit var exitFormHelper: ExitFormHelper

    @Before
    fun setUp() {
        exitFormHelper = ExitFormHelperImpl()
    }

    @Test
    fun `getExitFormFromModalities should return generic config if there are no modalities`() {
        val config = exitFormHelper.getExitFormFromModalities(listOf())
        assertThat(getTitleResFromConfig(config)).isEqualTo(R.string.why_did_you_skip_biometrics)
    }

    @Test
    fun `getExitFormFromModalities should return generic config if there are several modalities`() {
        val config = exitFormHelper.getExitFormFromModalities(listOf(
            GeneralConfiguration.Modality.FACE,
            GeneralConfiguration.Modality.FINGERPRINT
        ))
        assertThat(getTitleResFromConfig(config)).isEqualTo(R.string.why_did_you_skip_biometrics)
    }

    @Test
    fun `getExitFormFromModalities should return face config for face modality`() {
        val config = exitFormHelper.getExitFormFromModalities(listOf(GeneralConfiguration.Modality.FACE))
        assertThat(getTitleResFromConfig(config)).isEqualTo(R.string.why_did_you_skip_face_capture)
    }

    @Test
    fun `getExitFormFromModalities should return fingerprint config for fingerprint modality`() {
        val config = exitFormHelper.getExitFormFromModalities(listOf(GeneralConfiguration.Modality.FINGERPRINT))
        assertThat(getTitleResFromConfig(config)).isEqualTo(R.string.why_did_you_skip_fingerprinting)
    }

    @Test
    fun `test buildExitFormResponse should return null if data is empty`() {
        val exitFormData = ExitFormResult(false)
        val result = exitFormHelper.buildExitFormResponse(exitFormData)
        assertThat(result).isNull()
    }

    @Test
    fun `test buildExitFormResponse should return null if exit form was not submitted`() {
        val exitFormData = ExitFormResult(false)
        val result = exitFormHelper.buildExitFormResponse(exitFormData)
        assertThat(result).isNull()
    }

    @Test
    fun `test buildExitFormResponse should return CoreExitFormResponse if exit form was submitted`() {
        val exitFormData = ExitFormResult(true, ExitFormOption.Other)
        val result = exitFormHelper.buildExitFormResponse(exitFormData)
        assertThat(result).isNotNull()
        assertThat((result as ExitFormResponse).reason).isEqualTo(ExitFormReason.OTHER)
    }

    // Slightly leaking implementation details here,
    // but there is no other way to check if correct config was made
    private fun getTitleResFromConfig(config: Bundle): Int? =
        ExitFormFragmentArgs.fromBundle(config).exitFormConfiguration.titleRes

}
