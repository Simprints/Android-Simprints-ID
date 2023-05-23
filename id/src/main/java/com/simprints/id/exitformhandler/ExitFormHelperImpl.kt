package com.simprints.id.exitformhandler

import android.os.Bundle
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.exitform.exitFormConfiguration
import com.simprints.feature.exitform.scannerOptions
import com.simprints.feature.exitform.toArgs
import com.simprints.id.data.exitform.ExitFormReason.Companion.fromExitFormOption
import com.simprints.id.orchestrator.steps.core.response.CoreResponse
import com.simprints.id.orchestrator.steps.core.response.ExitFormResponse
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.resources.R
import javax.inject.Inject

class ExitFormHelperImpl @Inject constructor() : ExitFormHelper {

    override fun getExitFormFromModalities(modalities: List<GeneralConfiguration.Modality>): Bundle = when {
        modalities.size != 1 -> exitFormConfiguration {
            titleRes = R.string.why_did_you_skip_biometrics
            backButtonRes = R.string.button_scan_prints
        }

        modalities.first() == GeneralConfiguration.Modality.FACE -> exitFormConfiguration {
            titleRes = R.string.why_did_you_skip_face_capture
            backButtonRes = R.string.exit_form_capture_face
        }

        else -> exitFormConfiguration {
            titleRes = R.string.why_did_you_skip_fingerprinting
            backButtonRes = R.string.button_scan_prints
            visibleOptions = scannerOptions()
        }
    }.toArgs()

    override fun buildExitFormResponse(result: ExitFormResult): CoreResponse? = result.submittedOption()
        ?.let { ExitFormResponse(fromExitFormOption(it), result.reason.orEmpty()) }
}
