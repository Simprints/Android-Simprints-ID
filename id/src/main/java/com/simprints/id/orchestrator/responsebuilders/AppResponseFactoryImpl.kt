package com.simprints.id.orchestrator.responsebuilders

import com.simprints.core.tools.time.TimeHelper
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.*
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFollowUp.AppConfirmIdentityRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFollowUp.AppEnrolLastBiometricsRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.EnrolmentHelper
import com.simprints.id.orchestrator.responsebuilders.adjudication.EnrolAdjudicationAction
import com.simprints.id.orchestrator.responsebuilders.adjudication.EnrolResponseAdjudicationHelper
import com.simprints.id.orchestrator.steps.Step
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.config.domain.models.ProjectConfiguration

class AppResponseFactoryImpl(
    private val enrolmentHelper: EnrolmentHelper,
    private val timeHelper: TimeHelper,
    private val configManager: ConfigManager,
    private val enrolResponseAdjudicationHelper: EnrolResponseAdjudicationHelper
) : AppResponseFactory {

    override suspend fun buildAppResponse(
        modalities: List<GeneralConfiguration.Modality>,
        appRequest: AppRequest,
        steps: List<Step>,
        sessionId: String
    ): AppResponse {
        val projectConfiguration = configManager.getProjectConfiguration()
        return when (appRequest) {
            is AppEnrolRequest -> performAdjudicationForEnrolRequest(
                projectConfiguration,
                steps
            ).run {
                when (this) {
                    EnrolAdjudicationAction.ENROL -> AppResponseBuilderForEnrol(
                        enrolmentHelper,
                        timeHelper
                    )
                    EnrolAdjudicationAction.IDENTIFY -> AppResponseBuilderForIdentify(
                        projectConfiguration
                    )
                }
            }
            is AppIdentifyRequest -> AppResponseBuilderForIdentify(projectConfiguration)
            is AppVerifyRequest -> AppResponseBuilderForVerify(projectConfiguration)
            is AppConfirmIdentityRequest -> AppResponseBuilderForConfirmIdentity()
            is AppEnrolLastBiometricsRequest -> AppResponseBuilderForEnrolLastBiometrics()
        }.buildAppResponse(modalities, appRequest, steps, sessionId)
    }

    private fun performAdjudicationForEnrolRequest(
        projectConfiguration: ProjectConfiguration,
        steps: List<Step>
    ) =
        enrolResponseAdjudicationHelper.getAdjudicationAction(
            projectConfiguration,
            steps
        )
}

