package com.simprints.id.exitformhandler

import android.content.Intent
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.core.response.CoreResponse

interface ExitFormHelper {
    fun buildExitFormResponseForCore(data: Intent?): CoreResponse?

    fun getExitFormActivityClassFromModalities(modalities: List<Modality>): String?
}
