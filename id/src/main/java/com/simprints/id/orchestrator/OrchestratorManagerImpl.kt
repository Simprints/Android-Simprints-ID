package com.simprints.id.orchestrator

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import com.simprints.id.activities.dashboard.cards.daily_activity.repository.DashboardDailyActivityRepository
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.modality.Modality.FINGER
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.*
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFollowUp.AppConfirmIdentityRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFollowUp.AppEnrolLastBiometricsRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.orchestrator.FlowProvider.FlowType.*
import com.simprints.id.orchestrator.cache.HotCache
import com.simprints.id.orchestrator.modality.ModalityFlow
import com.simprints.id.orchestrator.responsebuilders.AppResponseFactory
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.ONGOING

open class OrchestratorManagerImpl(
    private val flowModalityFactory: ModalityFlowFactory,
    private val appResponseFactory: AppResponseFactory,
    private val hotCache: HotCache,
    private val dashboardDailyActivityRepository: DashboardDailyActivityRepository,
    private val personCreationEventHelper: PersonCreationEventHelper
) : OrchestratorManager, FlowProvider {

    override val ongoingStep = MutableLiveData<Step?>()
    override val appResponse = MutableLiveData<AppResponse?>()

    internal lateinit var modalities: List<Modality>
    internal var sessionId: String = ""

    private lateinit var modalitiesFlow: ModalityFlow

    override suspend fun initialise(modalities: List<Modality>,
                                    appRequest: AppRequest,
                                    sessionId: String) {
        this.sessionId = sessionId
        hotCache.appRequest = appRequest
        this.modalities = modalities
        modalitiesFlow = flowModalityFactory.createModalityFlow(appRequest, modalities)
        resetInternalState()

        proceedToNextStepOrAppResponse()
    }

    override suspend fun handleIntentResult(appRequest: AppRequest, requestCode: Int, resultCode: Int, data: Intent?) {
        modalitiesFlow.handleIntentResult(appRequest, requestCode, resultCode, data)
        val fingerprintCaptureCompleted =
            !modalities.contains(FINGER) || modalitiesFlow.steps.any { it.request is FingerprintCaptureRequest && it.getResult() is FingerprintCaptureResponse }

        val faceCaptureCompleted =
            !modalities.contains(FACE) || modalitiesFlow.steps.any { it.request is FingerprintCaptureRequest && it.getResult() is FaceCaptureResponse }


        if (fingerprintCaptureCompleted && faceCaptureCompleted) {
            personCreationEventHelper.addPersonCreationEventIfNeeded(modalitiesFlow.steps.mapNotNull { it.getResult() })
        }

        proceedToNextStepOrAppResponse()
    }

    override suspend fun restoreState() {
        resetInternalState()
        hotCache.load().let(modalitiesFlow::restoreState)
        proceedToNextStepOrAppResponse()
    }

    override fun clearState() {
        hotCache.clearSteps()
    }

    override fun saveState() {
        hotCache.clearSteps()
        modalitiesFlow.steps.forEach {
            hotCache.save(it)
        }
    }

    override fun getCurrentFlow() =
        when (hotCache.appRequest) {
            is AppEnrolRequest -> ENROL
            is AppIdentifyRequest -> IDENTIFY
            is AppVerifyRequest -> VERIFY
            is AppEnrolLastBiometricsRequest -> throw IllegalStateException("Not running one of the main flows")
            is AppConfirmIdentityRequest -> throw IllegalStateException("Not running one of the main flows")
        }

    private suspend fun proceedToNextStepOrAppResponse() {
        with(modalitiesFlow) {
            if (!anyStepOngoing()) {
                val potentialNextStep = getNextStepToLaunch()
                if (potentialNextStep != null) {
                    startStep(potentialNextStep)
                } else {
                    buildAppResponseAndUpdateDailyActivity()
                }
            }
        }
    }

    private fun startStep(step: Step) {
        step.setStatus(ONGOING)
        ongoingStep.value = step
        appResponse.value = null
    }

    private fun ModalityFlow.anyStepOngoing() = steps.any { it.getStatus() == ONGOING }

    private suspend fun buildAppResponseAndUpdateDailyActivity() {
        val steps = modalitiesFlow.steps
        val appResponseToReturn = appResponseFactory.buildAppResponse(
            modalities, hotCache.appRequest, steps, sessionId
        )

        ongoingStep.value = null
        appResponse.value = appResponseToReturn
        dashboardDailyActivityRepository.updateDailyActivity(appResponseToReturn)
    }

    private fun resetInternalState() {
        appResponse.value = null
        ongoingStep.value = null
    }

}
