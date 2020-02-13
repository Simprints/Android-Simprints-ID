package com.simprints.id.orchestrator

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import com.simprints.id.activities.dashboard.cards.daily_activity.repository.DashboardDailyActivityRepository
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequestType
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.cache.HotCache
import com.simprints.id.orchestrator.modality.ModalityFlow
import com.simprints.id.orchestrator.responsebuilders.AppResponseFactory
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.ONGOING

open class OrchestratorManagerImpl(
    private val flowModalityFactory: ModalityFlowFactory,
    private val appResponseFactory: AppResponseFactory,
    private val hotCache: HotCache,
    private val dashboardDailyActivityRepository: DashboardDailyActivityRepository
) : OrchestratorManager, FlowProvider {

    override val ongoingStep = MutableLiveData<Step?>()
    override val appResponse = MutableLiveData<AppResponse?>()

    internal lateinit var modalities: List<Modality>
    internal lateinit var appRequest: AppRequest
    internal var sessionId: String = ""

    private lateinit var modalitiesFlow: ModalityFlow
    private lateinit var flow: AppRequestType

    override suspend fun initialise(modalities: List<Modality>,
                                    appRequest: AppRequest,
                                    sessionId: String) {
        this.sessionId = sessionId
        this.appRequest = appRequest
        this.modalities = modalities
        modalitiesFlow = flowModalityFactory.createModalityFlow(appRequest, modalities)
        resetInternalState()
        flow = appRequest.type

        proceedToNextStepOrAppResponse()
    }

    override suspend fun handleIntentResult(appRequest: AppRequest, requestCode: Int, resultCode: Int, data: Intent?) {
        modalitiesFlow.handleIntentResult(appRequest, requestCode, resultCode, data)
        proceedToNextStepOrAppResponse()
    }

    override suspend fun restoreState() {
        resetInternalState()
        hotCache.load().let(modalitiesFlow::restoreState)
        proceedToNextStepOrAppResponse()
    }

    override fun clearState() {
        hotCache.clear()
    }

    override fun saveState() {
        hotCache.clear()
        modalitiesFlow.steps.forEach {
            hotCache.save(it)
        }
    }

    override fun getCurrentFlow() = flow

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
            modalities, appRequest, steps, sessionId
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
