package com.simprints.feature.orchestrator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.matcher.MatchParams
import com.simprints.feature.orchestrator.cache.OrchestratorCache
import com.simprints.feature.orchestrator.model.OrchestratorResult
import com.simprints.feature.orchestrator.steps.MatchStepStubPayload
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.feature.orchestrator.steps.StepId
import com.simprints.feature.orchestrator.steps.StepStatus
import com.simprints.feature.orchestrator.usecases.steps.BuildStepsUseCase
import com.simprints.feature.orchestrator.usecases.AddCallbackEventUseCase
import com.simprints.feature.orchestrator.usecases.response.AppResponseBuilderUseCase
import com.simprints.feature.orchestrator.usecases.CreatePersonEventUseCase
import com.simprints.feature.orchestrator.usecases.MapRefusalOrErrorResultUseCase
import com.simprints.feature.orchestrator.usecases.ShouldCreatePersonUseCase
import com.simprints.feature.orchestrator.usecases.UpdateDailyActivityUseCase
import com.simprints.feature.setup.LocationStore
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.logging.Simber
import com.simprints.infra.orchestration.data.ActionRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
internal class OrchestratorViewModel @Inject constructor(
    private val configManager: ConfigManager,
    private val cache: OrchestratorCache,
    private val locationStore: LocationStore,
    private val stepsBuilder: BuildStepsUseCase,
    private val mapRefusalOrErrorResult: MapRefusalOrErrorResultUseCase,
    private val shouldCreatePerson: ShouldCreatePersonUseCase,
    private val createPersonEvent: CreatePersonEventUseCase,
    private val appResponseBuilder: AppResponseBuilderUseCase,
    private val addCallbackEvent: AddCallbackEventUseCase,
    private val updateDailyActivity: UpdateDailyActivityUseCase,
) : ViewModel() {

    private var modalities = emptySet<GeneralConfiguration.Modality>()
    private var steps = emptyList<Step>()
    private var actionRequest: ActionRequest? = null

    val currentStep: LiveData<LiveDataEventWithContent<Step?>>
        get() = _currentStep
    private val _currentStep = MutableLiveData<LiveDataEventWithContent<Step?>>()

    val appResponse: LiveData<LiveDataEventWithContent<OrchestratorResult>>
        get() = _appResponse
    private val _appResponse = MutableLiveData<LiveDataEventWithContent<OrchestratorResult>>()

    fun handleAction(action: ActionRequest) = viewModelScope.launch {
        val projectConfiguration = configManager.getProjectConfiguration()

        modalities = projectConfiguration.general.modalities.toSet()
        steps = stepsBuilder.build(action, projectConfiguration)

        actionRequest = action

        doNextStep()
    }

    fun handleResult(result: Serializable) {
        Simber.i(result.toString())
        val errorResponse = mapRefusalOrErrorResult(result)
        if (errorResponse != null) {
            // Shortcut the flow execution if any refusal or error result is found
            addCallbackEvent(errorResponse)
            _appResponse.send(OrchestratorResult(actionRequest, errorResponse))
            return
        }

        steps.firstOrNull { it.status == StepStatus.IN_PROGRESS }?.let {
            it.status = StepStatus.COMPLETED
            it.result = result

            updateMatcherStepPayload(it, result)
        }

        if (shouldCreatePerson(actionRequest, modalities, steps)) {
            viewModelScope.launch { createPersonEvent(steps.mapNotNull { it.result }) }
        }

        doNextStep()
    }

    override fun onCleared() {
        super.onCleared()
        cache.steps = steps
    }

    private fun doNextStep() {
        if (steps.all { it.status != StepStatus.IN_PROGRESS }) {
            val nextStep = steps.firstOrNull { it.status == StepStatus.NOT_STARTED }
            if (nextStep != null) {
                nextStep.status = StepStatus.IN_PROGRESS
                _currentStep.send(nextStep)
            } else {
                // Acquiring location info could take long time, so we should stop location tracker
                // before returning to the caller app to avoid creating empty sessions.
                locationStore.cancelLocationCollection()
                buildAppResponse()
            }
        }
    }

    private fun buildAppResponse() = viewModelScope.launch {
        val projectConfiguration = configManager.getProjectConfiguration()
        val cachedActionRequest = actionRequest
        val appResponse = appResponseBuilder(
            projectConfiguration,
            cachedActionRequest,
            steps.mapNotNull { it.result },
        )

        updateDailyActivity(appResponse)
        addCallbackEvent(appResponse)
        _appResponse.send(OrchestratorResult(cachedActionRequest, appResponse))
    }

    private fun updateMatcherStepPayload(currentStep: Step, result: Serializable) {
        if (currentStep.id == StepId.FACE_CAPTURE && result is FaceCaptureResult) {
            val matchingStep = steps.firstOrNull { it.id == StepId.FACE_MATCHER }

            if (matchingStep != null) {
                val faceSamples = result.results.mapNotNull { it.sample }
                    .map { MatchParams.FaceSample(it.faceId, it.template) }
                //TODO: check
                val newPayload = matchingStep.payload
                    .getParcelable<MatchStepStubPayload>(MatchStepStubPayload.STUB_KEY)
                    ?.toFaceStepArgs(faceSamples)

                if (newPayload != null) {
                    matchingStep.payload = newPayload
                }
            }
        }
        if (currentStep.id == StepId.FINGERPRINT_CAPTURE && result is FingerprintCaptureResult) {
            val matchingStep = steps.firstOrNull { it.id == StepId.FINGERPRINT_MATCHER }

            if (matchingStep != null) {
                val fingerprintSamples = result.results.mapNotNull { it.sample }
                    .map { MatchParams.FingerprintSample(it.fingerIdentifier, it.format, it.template) }
                //TODO: check
                val newPayload = matchingStep.payload
                    .getParcelable<MatchStepStubPayload>(MatchStepStubPayload.STUB_KEY)
                    ?.toFingerprintStepArgs(fingerprintSamples)

                if (newPayload != null) {
                    matchingStep.payload = newPayload
                }
            }
        }
    }
}
