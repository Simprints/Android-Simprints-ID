package com.simprints.id.exitformhandler

import android.content.Intent
import com.simprints.id.domain.moduleapi.core.response.CoreResponse

interface ExitFormHandler {
    fun buildExitFormResposeForCore(data: Intent?): CoreResponse?
}
