package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.tools.TimeHelper

class AppResponseFactoryImpl(
    private val personRepository: PersonRepository,
    private val sessionEventsManager: SessionEventsManager,
    private val timeHelper: TimeHelper
) : AppResponseFactory {

    override suspend fun buildAppResponse(modalities: List<Modality>,
                                          appRequest: AppRequest,
                                          steps: List<Step>,
                                          sessionId: String): AppResponse =
        /**
         * Currently only FINGER/AppResponseBuilderForFinger is used. The others
         * are placeholders for when we will introduce the FaceModality
         */
        when (appRequest) {
            is AppEnrolRequest -> AppResponseBuilderForEnrol(personRepository, sessionEventsManager, timeHelper)
            is AppIdentifyRequest -> AppResponseBuilderForIdentify()
            is AppVerifyRequest -> AppResponseBuilderForVerify()
            else -> null
        }?.buildAppResponse(modalities, appRequest, steps, sessionId)
            ?: throw Throwable("Wrong modalities")
}

