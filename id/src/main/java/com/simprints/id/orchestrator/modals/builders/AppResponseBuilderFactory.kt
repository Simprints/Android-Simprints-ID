package com.simprints.id.orchestrator.modals.builders

import com.simprints.id.domain.modal.Modal
import com.simprints.id.domain.modal.ModalResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse

class AppResponseBuilderForModalImpl: AppResponseBuilderForModal {

    override fun buildResponse(modal: Modal,
                               appRequest: AppRequest,
                               modalResponses: List<ModalResponse>,
                               sessionId: String): AppResponse {

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
