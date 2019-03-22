package com.simprints.id.orchestrator

import android.content.Intent
import com.simprints.id.domain.modal.ModalResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse

interface ModalFlow {

    var results: MutableList<ModalResponse>

    fun getIntent(): ModalStepRequest

    @Throws(IllegalArgumentException::class)
    fun handleModalResultAndGetPotentialFinalAppResponse(requestCode: Int, resultCode: Int, data: Intent?): AppResponse?
}
