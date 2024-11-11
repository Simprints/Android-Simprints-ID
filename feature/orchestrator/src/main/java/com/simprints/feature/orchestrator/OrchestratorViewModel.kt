package com.simprints.feature.orchestrator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.module.SimpleModule
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.serialization.TokenizationClassNameDeserializer
import com.simprints.core.domain.tokenization.serialization.TokenizationClassNameSerializer
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.json.JsonHelper
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.enrollast.EnrolLastBiometricContract
import com.simprints.feature.enrollast.EnrolLastBiometricParams
import com.simprints.feature.orchestrator.cache.OrchestratorCache
import com.simprints.feature.orchestrator.exceptions.SubjectAgeNotSupportedException
import com.simprints.feature.orchestrator.model.OrchestratorResult
import com.simprints.feature.orchestrator.steps.MatchStepStubPayload
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.feature.orchestrator.steps.StepId
import com.simprints.feature.orchestrator.steps.StepStatus
import com.simprints.feature.orchestrator.usecases.AddCallbackEventUseCase
import com.simprints.feature.orchestrator.usecases.CreatePersonEventUseCase
import com.simprints.feature.orchestrator.usecases.MapRefusalOrErrorResultUseCase
import com.simprints.feature.orchestrator.usecases.MapStepsForLastBiometricEnrolUseCase
import com.simprints.feature.orchestrator.usecases.ShouldCreatePersonUseCase
import com.simprints.feature.orchestrator.usecases.UpdateDailyActivityUseCase
import com.simprints.feature.orchestrator.usecases.response.AppResponseBuilderUseCase
import com.simprints.feature.orchestrator.usecases.steps.BuildStepsUseCase
import com.simprints.feature.selectagegroup.SelectSubjectAgeGroupResult
import com.simprints.feature.setup.LocationStore
import com.simprints.fingerprint.capture.FingerprintCaptureParams
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.logging.Simber
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import com.simprints.infra.orchestration.data.responses.AppResponse
import com.simprints.matcher.MatchParams
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
    private val mapStepsForLastBiometrics: MapStepsForLastBiometricEnrolUseCase,
) : ViewModel() {

    var isRequestProcessed = false
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
        actionRequest = action

        try {
            // In case of a follow-up action, we should restore completed steps from cache
            // and add new ones to the list. This way all session steps are available throughout
            // the app for reference (i.e. have we already captured face in this session?)
            steps = cache.steps + stepsBuilder.build(action, projectConfiguration)
        } catch (_: SubjectAgeNotSupportedException) {
            handleErrorResponse(AppErrorResponse(AppErrorReason.AGE_GROUP_NOT_SUPPORTED))
            return@launch
        }

        doNextStep()
    }

    fun handleResult(result: Serializable) = viewModelScope.launch {
        Simber.d(result.toString())

        val projectConfiguration = configManager.getProjectConfiguration()
        val errorResponse = mapRefusalOrErrorResult(result, projectConfiguration)
        if (errorResponse != null) {
            // Shortcut the flow execution if any refusal or error result is found
            handleErrorResponse(errorResponse)
            return@launch
        }

        steps.firstOrNull { it.status == StepStatus.IN_PROGRESS }?.let { step ->
            step.status = StepStatus.COMPLETED
            step.result = result

            updateMatcherStepPayload(step, result)
        }

        if (shouldCreatePerson(actionRequest, modalities, steps)) {
            createPersonEvent(steps.mapNotNull { it.result })
        }

        if (result is SelectSubjectAgeGroupResult) {
            val captureAndMatchSteps = stepsBuilder.buildCaptureAndMatchStepsForAgeGroup(
                actionRequest!!,
                projectConfiguration,
                result.ageGroup
            )
            steps = steps + captureAndMatchSteps
        }

        doNextStep()
    }

    fun handleErrorResponse(errorResponse: AppResponse) {
        addCallbackEvent(errorResponse)
        _appResponse.send(OrchestratorResult(actionRequest, errorResponse))
    }

    fun restoreStepsIfNeeded() {
        if (steps.isEmpty()) {
            // Restore the steps from cache
            steps = cache.steps
        }
    }

    fun restoreModalitiesIfNeeded() {
        viewModelScope.launch {
            if (modalities.isEmpty()) {
                val projectConfiguration = configManager.getProjectConfiguration()
                modalities = projectConfiguration.general.modalities.toSet()
            }
        }
    }

    override fun onCleared() {
        cache.steps = steps
        super.onCleared()
    }

    private fun doNextStep() {
        if (steps.all { it.status != StepStatus.IN_PROGRESS }) {
            val nextStep = steps.firstOrNull { it.status == StepStatus.NOT_STARTED }
            if (nextStep != null) {
                updateEnrolLastBiometricParamsIfNeeded(nextStep)
                nextStep.status = StepStatus.IN_PROGRESS
                cache.steps = steps
                _currentStep.send(nextStep)
            } else {
                // Acquiring location info could take long time, so we should stop location tracker
                // before returning to the caller app to avoid creating empty sessions.
                locationStore.cancelLocationCollection()
                cache.steps = steps
                buildAppResponse()
            }
        }
    }

    /**
     * Update the enrol last biometric params in case there were more steps executed in the current flow.
     */
    private fun updateEnrolLastBiometricParamsIfNeeded(step: Step) {
        if (step.id == StepId.ENROL_LAST_BIOMETRIC) {
            step.payload.getParcelable<EnrolLastBiometricParams>("params")?.let { params ->
                val updatedParams = params.copy(
                    steps = mapStepsForLastBiometrics(steps.mapNotNull { it.result })
                )
                step.payload = EnrolLastBiometricContract.getArgs(
                    projectId = updatedParams.projectId,
                    userId = updatedParams.userId,
                    moduleId = updatedParams.moduleId,
                    steps = updatedParams.steps,
                )
            }
        }
    }

    private fun buildAppResponse() = viewModelScope.launch {
        val projectConfiguration = configManager.getProjectConfiguration()
        val appResponse = appResponseBuilder(
            projectConfiguration,
            actionRequest,
            steps.mapNotNull { it.result },
        )

        updateDailyActivity(appResponse)
        addCallbackEvent(appResponse)
        _appResponse.send(OrchestratorResult(actionRequest, appResponse))
    }

    private fun updateMatcherStepPayload(currentStep: Step, result: Serializable) {
        if (currentStep.id == StepId.FACE_CAPTURE && result is FaceCaptureResult) {
            val matchingStep = steps.firstOrNull { it.id == StepId.FACE_MATCHER }

            if (matchingStep != null) {
                val faceSamples = result.results.mapNotNull { it.sample }
                    .map { MatchParams.FaceSample(it.faceId, it.template) }
                val newPayload = matchingStep.payload
                    .getParcelable<MatchStepStubPayload>(MatchStepStubPayload.STUB_KEY)
                    ?.toFaceStepArgs(faceSamples)

                if (newPayload != null) {
                    matchingStep.payload = newPayload
                }
            }
        }
        if (currentStep.id == StepId.FINGERPRINT_CAPTURE && result is FingerprintCaptureResult) {
            val captureParams = currentStep.payload.getParcelable<FingerprintCaptureParams>("params")
            // Find the matching step for the same fingerprint SDK as there may be multiple match steps
            val matchingStep = steps.firstOrNull { step ->
                if (step.id != StepId.FINGERPRINT_MATCHER) {
                    false
                }
                else {
                    val stepSdk = step.payload.getParcelable<MatchStepStubPayload>(MatchStepStubPayload.STUB_KEY)?.fingerprintSDK
                    stepSdk == captureParams?.fingerprintSDK
                }
            }

            if (matchingStep != null) {
                val fingerprintSamples = result.results.mapNotNull { it.sample }
                    .map {
                        MatchParams.FingerprintSample(
                            fingerId = it.fingerIdentifier,
                            format = it.format,
                            template = it.template
                        )
                    }
                val newPayload = matchingStep.payload
                    .getParcelable<MatchStepStubPayload>(MatchStepStubPayload.STUB_KEY)
                    ?.toFingerprintStepArgs(fingerprintSamples)

                if (newPayload != null) {
                    matchingStep.payload = newPayload
                }
            }
        }
    }

    fun setActionRequestFromJson(json: String) {
        try {
            actionRequest = JsonHelper.fromJson(
                json = json,
                module = dbSerializationModule,
                type = object : TypeReference<ActionRequest>() {})
        } catch (e: Exception) {
            Simber.e(e)
        }
    }

    fun getActionRequestJson(): String? {
        return try {
            actionRequest?.let {
                JsonHelper.toJson(it, dbSerializationModule)
            }
        } catch (e: Exception) {
            Simber.e(e)
            null
        }
    }

    companion object {
        val dbSerializationModule = SimpleModule().apply {
            addSerializer(TokenizableString::class.java, TokenizationClassNameSerializer())
            addDeserializer(TokenizableString::class.java, TokenizationClassNameDeserializer())
        }
    }
}
