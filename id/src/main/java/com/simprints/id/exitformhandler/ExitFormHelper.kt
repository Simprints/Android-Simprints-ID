package com.simprints.id.exitformhandler

import android.os.Bundle
import com.simprints.id.orchestrator.steps.core.response.CoreResponse
import com.simprints.infra.config.domain.models.GeneralConfiguration

interface ExitFormHelper {
    fun getExitFormFromModalities(modalities: List<GeneralConfiguration.Modality>): Bundle

    fun buildExitFormResponse(data: Bundle): CoreResponse?
}
