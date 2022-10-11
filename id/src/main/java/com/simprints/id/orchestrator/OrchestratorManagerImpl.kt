package com.simprints.id.orchestrator

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkManager
import com.simprints.core.domain.common.FlowProvider
import com.simprints.core.domain.common.FlowProvider.FlowType.*
import com.simprints.id.activities.dashboard.cards.daily_activity.repository.DashboardDailyActivityRepository
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.*
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFollowUp.AppConfirmIdentityRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFollowUp.AppEnrolLastBiometricsRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.orchestrator.cache.HotCache
import com.simprints.id.orchestrator.modality.ModalityFlow
import com.simprints.id.orchestrator.responsebuilders.AppResponseFactory
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.ONGOING
import com.simprints.id.services.location.STORE_USER_LOCATION_WORKER_TAG
import com.simprints.infra.config.domain.models.GeneralConfiguration
import splitties.init.appCtx

open class OrchestratorManagerImpl(
    private val flowModalityFactory: ModalityFlowFactory,
    private val appResponseFactory: AppResponseFactory,
    private val hotCache: HotCache,
    private val dashboardDailyActivityRepository: DashboardDailyActivityRepository,
    private val personCreationEventHelper: PersonCreationEventHelper
) : OrchestratorManager, FlowProvider {

    override val ongoingStep = MutableLiveData<Step?>()
    override val appResponse = MutableLiveData<AppResponse?>()

    internal lateinit var modalities: List<GeneralConfiguration.Modality>
    internal var sessionId: String = ""

    private lateinit var modalitiesFlow: ModalityFlow

    override suspend fun initialise(
        modalities: List<GeneralConfiguration.Modality>,
        appRequest: AppRequest,
        sessionId: String
    ) {
        this.sessionId = sessionId
        hotCache.appRequest = appRequest
        this.modalities = modalities
        modalitiesFlow = flowModalityFactory.createModalityFlow(appRequest)
        resetInternalState()

        proceedToNextStepOrAppResponse()
    }

    override suspend fun handleIntentResult(
        appRequest: AppRequest,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        modalitiesFlow.handleIntentResult(appRequest, requestCode, resultCode, data)

        if (appRequest !is AppRequest.AppRequestFollowUp) {
            val fingerprintCaptureCompleted =
                !modalities.contains(GeneralConfiguration.Modality.FINGERPRINT) || modalitiesFlow.steps.filter { it.request is FingerprintCaptureRequest }
                    .all { it.getResult() is FingerprintCaptureResponse }

            val faceCaptureCompleted =
                !modalities.contains(GeneralConfiguration.Modality.FACE) || modalitiesFlow.steps.filter { it.request is FaceCaptureRequest }
                    .all { it.getResult() is FaceCaptureResponse }


            if (fingerprintCaptureCompleted && faceCaptureCompleted) {
                personCreationEventHelper.addPersonCreationEventIfNeeded(modalitiesFlow.steps.mapNotNull { it.getResult() })
            }
        }

        proceedToNextStepOrAppResponse()
    }

    override suspend fun restoreState() {
        resetInternalState()
        hotCache.load().let(modalitiesFlow::restoreState)
        proceedToNextStepOrAppResponse()
    }


    override fun saveState() {
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
                    // Acquiring location info could take long time. so we should cancel StoreUserLocationIntoCurrentSessionWorker
                    // before returning to the caller app to avoid creating empty sessions.
                    WorkManager.getInstance(appCtx)
                        .cancelAllWorkByTag(STORE_USER_LOCATION_WORKER_TAG)
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

        dashboardDailyActivityRepository.updateDailyActivity(appResponseToReturn)
        ongoingStep.value = null
        appResponse.value = appResponseToReturn
    }

    private fun resetInternalState() {
        appResponse.value = null
        ongoingStep.value = null
    }

}
