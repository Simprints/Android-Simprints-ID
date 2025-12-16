package com.simprints.feature.orchestrator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.module.SimpleModule
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.capture.BiometricReferenceCapture
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.core.domain.step.StepResult
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.serialization.TokenizationClassNameDeserializer
import com.simprints.core.domain.tokenization.serialization.TokenizationClassNameSerializer
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.json.JsonHelper
import com.simprints.face.capture.FaceCaptureParams
import com.simprints.feature.enrollast.EnrolLastBiometricContract
import com.simprints.feature.enrollast.EnrolLastBiometricParams
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import com.simprints.feature.orchestrator.cache.OrchestratorCache
import com.simprints.feature.orchestrator.exceptions.SubjectAgeNotSupportedException
import com.simprints.feature.orchestrator.model.OrchestratorResult
import com.simprints.feature.orchestrator.steps.MatchStepStubPayload
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.feature.orchestrator.steps.StepId
import com.simprints.feature.orchestrator.steps.StepStatus
import com.simprints.feature.orchestrator.usecases.AddCallbackEventUseCase
import com.simprints.feature.orchestrator.usecases.MapRefusalOrErrorResultUseCase
import com.simprints.feature.orchestrator.usecases.MapStepsForLastBiometricEnrolUseCase
import com.simprints.feature.orchestrator.usecases.UpdateDailyActivityUseCase
import com.simprints.feature.orchestrator.usecases.response.AppResponseBuilderUseCase
import com.simprints.feature.orchestrator.usecases.steps.BuildStepsUseCase
import com.simprints.feature.selectagegroup.SelectSubjectAgeGroupResult
import com.simprints.feature.setup.LocationStore
import com.simprints.fingerprint.capture.FingerprintCaptureParams
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import com.simprints.infra.orchestration.data.responses.AppResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.Serializable
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
internal class OrchestratorViewModel @Inject constructor(
    private val configManager: ConfigManager,
    private val cache: OrchestratorCache,
    private val locationStore: LocationStore,
    private val stepsBuilder: BuildStepsUseCase,
    private val mapRefusalOrErrorResult: MapRefusalOrErrorResultUseCase,
    private val appResponseBuilder: AppResponseBuilderUseCase,
    private val addCallbackEvent: AddCallbackEventUseCase,
    private val updateDailyActivity: UpdateDailyActivityUseCase,
    private val mapStepsForLastBiometrics: MapStepsForLastBiometricEnrolUseCase,
) : ViewModel() {
    var isRequestProcessed = false

    // [MS-1127] MF-ID: during enrolment, the same 'subjectId' needs to be used during the entire workflow
    private val enrolmentSubjectId = UUID.randomUUID().toString()
    private var modalities = emptySet<Modality>()
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
            val cachedSteps = cache.steps
            val cachedExternalCredentialResponse = getCachedCredentialResponse(cachedSteps)
            steps = cachedSteps + stepsBuilder.build(
                action = action,
                projectConfiguration = projectConfiguration,
                enrolmentSubjectId = enrolmentSubjectId,
                cachedScannedCredential = cachedExternalCredentialResponse?.scannedCredential,
            )
            Simber.i("Steps to execute: ${steps.joinToString { it.id.toString() }}", tag = ORCHESTRATION)
        } catch (_: SubjectAgeNotSupportedException) {
            handleErrorResponse(AppErrorResponse(AppErrorReason.AGE_GROUP_NOT_SUPPORTED))
            return@launch
        }

        doNextStep()
    }

    private fun getCachedCredentialResponse(steps: List<Step>): ExternalCredentialSearchResult? {
        steps.forEach { step ->
            if (step.id == StepId.EXTERNAL_CREDENTIAL) {
                return step.result as? ExternalCredentialSearchResult
            }
        }
        return null
    }

    fun handleResult(result: StepResult) = viewModelScope.launch {
        Simber.i("Handling step result: ${result.javaClass.simpleName}", tag = ORCHESTRATION)
        Simber.d(result.toString(), tag = ORCHESTRATION)

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
            Simber.i("Completed step: ${step.id}", tag = ORCHESTRATION)

            updateMatcherStepPayload(step, result)
            updateExternalCredentialStepPayload(step, result)
        }

        if (result is SelectSubjectAgeGroupResult) {
            val captureAndMatchSteps = stepsBuilder.buildCaptureAndMatchStepsForAgeGroup(
                action = actionRequest!!,
                projectConfiguration = projectConfiguration,
                ageGroup = result.ageGroup,
                enrolmentSubjectId = enrolmentSubjectId,
            )
            steps = steps + captureAndMatchSteps
        }

        if (result is ExternalCredentialSearchResult) {
            removeMatcherStepIfRequired(result)
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
            Simber.i("Restored steps: ${steps.joinToString { it.id.toString() }}", tag = ORCHESTRATION)
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
                Simber.i("Next step: ${nextStep.id}", tag = ORCHESTRATION)
                updateEnrolLastBiometricParamsIfNeeded(nextStep)
                nextStep.status = StepStatus.IN_PROGRESS
                cache.steps = steps
                _currentStep.send(nextStep)
            } else {
                Simber.i("All steps complete", tag = ORCHESTRATION)
                // Acquiring location info could take long time, so we should stop location tracker
                // before returning to the caller app to avoid creating empty sessions.
                locationStore.cancelLocationCollection()
                cache.steps = steps
                buildAppResponse()
            }
        }
    }

    /**
     * Removes Matcher steps during [FlowType.IDENTIFY] flow if External Credential search found any match.
     */
    private fun removeMatcherStepIfRequired(result: ExternalCredentialSearchResult) {
        if (result.flowType == FlowType.IDENTIFY) {
            val confirmedVerifications = result.goodMatches.size
            if (confirmedVerifications > 0) {
                steps = steps.filterNot { it.id in listOf(StepId.FACE_MATCHER, StepId.FINGERPRINT_MATCHER) }
                Simber.i(
                    "Matcher steps removed because External Credential search verified [$confirmedVerifications] candidate(s). Flow type = [${result.flowType}]",
                    tag = ORCHESTRATION,
                )
            }
        }
    }

    /**
     * Update the enrol last biometric params in case there were more steps executed in the current flow.
     */
    private fun updateEnrolLastBiometricParamsIfNeeded(step: Step) {
        if (step.id == StepId.ENROL_LAST_BIOMETRIC) {
            step.params?.let { it as? EnrolLastBiometricParams }?.let { params ->
                val updatedParams = params.copy(
                    steps = mapStepsForLastBiometrics(steps.mapNotNull { it.result }),
                )
                val cachedExternalCredentialResponse = getCachedCredentialResponse(cache.steps)
                step.params = EnrolLastBiometricContract.getParams(
                    projectId = updatedParams.projectId,
                    userId = updatedParams.userId,
                    moduleId = updatedParams.moduleId,
                    steps = updatedParams.steps,
                    scannedCredential = cachedExternalCredentialResponse?.scannedCredential,
                )
            }
        }
    }

    private fun buildAppResponse() = viewModelScope.launch {
        val projectConfiguration = configManager.getProjectConfiguration()
        val project = configManager.getProject() ?: return@launch
        val appResponse = appResponseBuilder(
            projectConfiguration = projectConfiguration,
            request = actionRequest,
            results = steps.mapNotNull { it.result },
            project = project,
            enrolmentSubjectId = enrolmentSubjectId,
        )

        updateDailyActivity(appResponse)
        addCallbackEvent(appResponse)
        _appResponse.send(OrchestratorResult(actionRequest, appResponse))
    }

    private fun updateMatcherStepPayload(
        currentStep: Step,
        result: Serializable,
    ) {
        if (currentStep.id == StepId.FACE_CAPTURE && result is BiometricReferenceCapture) {
            val captureParams = currentStep.params?.let { it as? FaceCaptureParams }
            val matchingStep = steps.firstOrNull { step ->
                if (step.id != StepId.FACE_MATCHER) {
                    false
                } else {
                    val stepSdk = step.params?.let { it as? MatchStepStubPayload }?.bioSdk
                    stepSdk == captureParams?.faceSDK
                }
            }

            if (matchingStep != null) {
                val newPayload = matchingStep.params
                    ?.let { it as? MatchStepStubPayload }
                    ?.toFaceStepArgs(result)

                if (newPayload != null) {
                    matchingStep.params = newPayload
                }
            }
        }
        if (currentStep.id == StepId.FINGERPRINT_CAPTURE && result is BiometricReferenceCapture) {
            val captureParams = currentStep.params?.let { it as? FingerprintCaptureParams }
            // Find the matching step for the same fingerprint SDK as there may be multiple match steps
            val matchingStep = steps.firstOrNull { step ->
                if (step.id != StepId.FINGERPRINT_MATCHER) {
                    false
                } else {
                    val stepSdk = step.params?.let { it as? MatchStepStubPayload }?.bioSdk
                    stepSdk == captureParams?.fingerprintSDK
                }
            }

            if (matchingStep != null) {
                val newPayload = matchingStep.params
                    ?.let { it as? MatchStepStubPayload }
                    ?.toFingerprintStepArgs(result)

                if (newPayload != null) {
                    matchingStep.params = newPayload
                }
            }
        }
    }

    /**
     * Passes face or fingerprint samples to the External Credential search step
     */
    private fun updateExternalCredentialStepPayload(
        currentStep: Step,
        result: StepResult,
    ) {
        if (currentStep.id !in listOf(StepId.FACE_CAPTURE, StepId.FINGERPRINT_CAPTURE)) return
        if (result !is BiometricReferenceCapture) return

        val step = steps.firstOrNull { it.id == StepId.EXTERNAL_CREDENTIAL } ?: return
        val params = step.params as? ExternalCredentialParams ?: return
        step.params = params.copy(probeReferences = params.probeReferences + listOf(result))
    }

    fun setActionRequestFromJson(json: String) {
        try {
            actionRequest = JsonHelper.fromJson(
                json = json,
                module = dbSerializationModule,
                type = object : TypeReference<ActionRequest>() {},
            )
        } catch (e: Exception) {
            Simber.e("Action request deserialization failed", e, tag = ORCHESTRATION)
        }
    }

    fun getActionRequestJson(): String? = try {
        actionRequest?.let {
            JsonHelper.toJson(it, dbSerializationModule)
        }
    } catch (e: Exception) {
        Simber.e("Action request serialization failed", e, tag = ORCHESTRATION)
        null
    }

    companion object {
        val dbSerializationModule = SimpleModule().apply {
            addSerializer(TokenizableString::class.java, TokenizationClassNameSerializer())
            addDeserializer(TokenizableString::class.java, TokenizationClassNameDeserializer())
        }
    }
}
