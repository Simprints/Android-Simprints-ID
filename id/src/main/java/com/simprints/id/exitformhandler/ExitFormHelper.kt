package com.simprints.id.exitformhandler

import android.content.Intent
import com.simprints.id.orchestrator.steps.core.response.CoreResponse
import com.simprints.infra.config.domain.models.GeneralConfiguration

interface ExitFormHelper {
    fun buildExitFormResponseForCore(data: Intent?): CoreResponse?

    fun getExitFormActivityClassFromModalities(modalities: List<GeneralConfiguration.Modality>): String?
}
