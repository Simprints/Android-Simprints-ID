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
import com.simprints.core.tools.extentions.nullIfEmpty
import com.simprints.core.tools.json.JsonHelper
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.enrollast.EnrolLastBiometricContract
import com.simprints.feature.enrollast.EnrolLastBiometricParams
import com.simprints.feature.externalcredential.model.ExternalCredentialResponse
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
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import com.simprints.infra.orchestration.data.responses.AppResponse
import com.simprints.matcher.FaceMatchResult
import com.simprints.matcher.FingerprintMatchResult
import com.simprints.matcher.MatchContract
import com.simprints.matcher.MatchParams
import com.simprints.matcher.MatchResult
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

    // [MS-960] New enrolments require 'subjectId' to be generated before building app response
    private val enrolmentSubjectId = UUID.randomUUID().toString()
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
            val cachedSteps = cache.steps
            val cachedExternalCredentialResponse = getCachedCredentialResponse(cachedSteps)
            steps = cachedSteps + stepsBuilder.build(action, projectConfiguration, enrolmentSubjectId, cachedExternalCredentialResponse)
            Simber.i("Steps to execute: ${steps.joinToString { it.id.toString() }}", tag = ORCHESTRATION)
        } catch (_: SubjectAgeNotSupportedException) {
            handleErrorResponse(AppErrorResponse(AppErrorReason.AGE_GROUP_NOT_SUPPORTED))
            return@launch
        }

        doNextStep()
    }

    private fun getCachedCredentialResponse(steps: List<Step>): ExternalCredentialResponse? {
        steps.map { step ->
            if (step.id == StepId.EXTERNAL_CREDENTIAL) {
                return step.result as? ExternalCredentialResponse
            }
        }
        return null
    }

    fun handleResult(result: Serializable) = viewModelScope.launch {
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
            updateSearchAndVerifyMatcherParamsIfNeeded(step, result)
        }

        if (result is SelectSubjectAgeGroupResult) {
            val captureAndMatchSteps = stepsBuilder.buildCaptureAndMatchStepsForAgeGroup(
                actionRequest!!,
                projectConfiguration,
                enrolmentSubjectId = enrolmentSubjectId,
                result.ageGroup,
            )
            steps = steps + captureAndMatchSteps
        }

        if (result is MatchResult) {
            getOneToManyMatchStepIfNecessary(result, configManager.getProjectConfiguration())?.let { oneToManyMatchStep ->
                Simber.i("Falling back to 1:N biometric search because External Credential biometrics do not match scanned biometrics")
                steps = steps + oneToManyMatchStep
            }
        }

        doNextStep()
    }

    private fun getOneToManyMatchStepIfNecessary(matchResult: MatchResult, config: ProjectConfiguration): Step? {
        // [MS-984] 1:1 falls back to 1:N only during identification request
        if (actionRequest !is ActionRequest.IdentifyActionRequest)
            return null


        val matchStep = steps.lastOrNull { it.id == StepId.FACE_MATCHER || it.id == StepId.FINGERPRINT_MATCHER }?.copy() ?: return null
        val matchParams = matchStep.payload.getParcelable<MatchParams>("params") ?: return null
        val subjectId = matchParams.queryForCandidates.subjectId
        if(subjectId?.nullIfEmpty() == null){
            // 1:N match was already done
            return null
        }

        matchResult.results.firstOrNull()?.confidence?.let { confidence ->
            val decisionPolicy = when (matchResult) {
                is FaceMatchResult -> config.face?.decisionPolicy
                is FingerprintMatchResult -> config.fingerprint?.getSdkConfiguration(matchResult.sdk)?.decisionPolicy
                else -> null
            } ?: return null

            if (confidence > decisionPolicy.low) {
                Simber.i("1:1 Match step confidence [$confidence] is above low threshold of [${decisionPolicy.low}]. Not falling back to 1:N")
                return null
            }
        }

        // matching against 1:N
        val updatedSubjectQuery = matchParams.queryForCandidates.copy(
            subjectId = null
        )

        with(matchStep) {
            status = StepStatus.NOT_STARTED
            result = null
            payload = MatchContract.getArgs(
                referenceId = matchParams.probeReferenceId,
                fingerprintSamples = matchParams.probeFingerprintSamples,
                faceSamples = matchParams.probeFaceSamples,
                fingerprintSDK = matchParams.fingerprintSDK,
                flowType = matchParams.flowType,
                subjectQuery = updatedSubjectQuery,
                biometricDataSource = matchParams.biometricDataSource,
                shouldDisplay1toNFallbackMessage = true

            )
        }
        return matchStep
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
     * [Ms-960] If 'Search and Verify' feature is enabled, then during the Identification flow it is necessary to call the 'Matcher' step
     * with conditional params.
     *
     * When id of the [currentStep] is [StepId.FACE_MATCHER] is [StepId.FINGERPRINT_MATCHER], and the [result] of the previous step is
     * [ExternalCredentialSearchResponse], then the 'Matcher' step needs to be called for either 1:1 or 1:N match depending on the [result]
     *
     * - If [ExternalCredentialSearchResponse] contains subject id, it indicates that there is a subject ID in the local DB. In this case
     * the 'Matcher' step needs to run match against this particular subject ID (1:1)
     * - If [ExternalCredentialSearchResponse] does not contains subject id, it indicates that there is no subject ID associated with the
     * provided external credential in the local DB. In this case the 'Matcher' step needs to run match against all records (1:N)
     *
     * @param currentStep current step in the chain
     * @param result result of the current step
     */
    private fun updateSearchAndVerifyMatcherParamsIfNeeded(currentStep: Step, result: Serializable) {
        if (currentStep.id == StepId.EXTERNAL_CREDENTIAL && result is ExternalCredentialResponse.ExternalCredentialSearchResponse) {
            val foundSubjectId = result.subjectId?.nullIfEmpty()
            val matchingSteps = listOf(StepId.FACE_MATCHER, StepId.FINGERPRINT_MATCHER)
            val matchingStep = steps.firstOrNull { it.id in matchingSteps } ?: return
            val matchParams = matchingStep.payload.getParcelable<MatchParams>("params") ?: return

            // If associated Subject ID is found via external credential, matching against 1:1. Otherwise, matching against 1:N
            val updatedSubjectQuery = matchParams.queryForCandidates.copy(
                subjectId = foundSubjectId
            )

            matchingStep.payload = MatchContract.getArgs(
                referenceId = matchParams.probeReferenceId,
                fingerprintSamples = matchParams.probeFingerprintSamples,
                faceSamples = matchParams.probeFaceSamples,
                fingerprintSDK = matchParams.fingerprintSDK,
                flowType = matchParams.flowType,
                subjectQuery = updatedSubjectQuery,
                biometricDataSource = matchParams.biometricDataSource,
                shouldDisplay1toNFallbackMessage = false
            )
        }
    }

    /**
     * Update the enrol last biometric params in case there were more steps executed in the current flow.
     */
    private fun updateEnrolLastBiometricParamsIfNeeded(step: Step) {
        if (step.id == StepId.ENROL_LAST_BIOMETRIC) {
            step.payload.getParcelable<EnrolLastBiometricParams>("params")?.let { params ->
                val updatedParams = params.copy(
                    steps = mapStepsForLastBiometrics(steps.mapNotNull { it.result }),
                )
                step.payload = EnrolLastBiometricContract.getArgs(
                    projectId = updatedParams.projectId,
                    userId = updatedParams.userId,
                    moduleId = updatedParams.moduleId,
                    steps = updatedParams.steps,
                    externalCredentialId = updatedParams.externalCredentialId,
                    externalCredentialImagePath = updatedParams.externalCredentialImagePath

                )
            }
        }
    }

    private fun buildAppResponse() = viewModelScope.launch {
        val projectConfiguration = configManager.getProjectConfiguration()
        val project = configManager.getProject(projectConfiguration.projectId)
        val externalCredential = getCachedCredentialResponse(steps)?.externalCredential
        val shouldReturnSearchAndVerifyFlag = shouldReturnSearchAndVerifyFlag(steps)
        val appResponse = appResponseBuilder(
            projectConfiguration =
                projectConfiguration,
            request = actionRequest,
            results = steps.mapNotNull { it.result },
            project = project,
            enrolmentSubjectId = enrolmentSubjectId,
            externalCredential = externalCredential,
            shouldReturnSearchAndVerifyFlag = shouldReturnSearchAndVerifyFlag,
        )

        updateDailyActivity(appResponse)
        addCallbackEvent(appResponse)
        _appResponse.send(OrchestratorResult(actionRequest, appResponse))
    }

    private fun shouldReturnSearchAndVerifyFlag(steps: List<Step>): Boolean {
        // [MS-992] Only 'IdentifyActionRequest' require search & verify flag to be returned
        if (actionRequest !is ActionRequest.IdentifyActionRequest)
            return false

        val matchingSteps = listOf(StepId.FACE_MATCHER, StepId.FINGERPRINT_MATCHER)
        val matchingStep = steps.firstOrNull { it.id in matchingSteps } ?: return false
        val matchParams = matchingStep.payload.getParcelable<MatchParams>("params") ?: return false
        if (matchParams.queryForCandidates.subjectId?.nullIfEmpty() == null) {
            // [MS-992] Verifying that matching step received a single 'Subject ID' to match against. This check eliminates possibility
            // of returning a positive flag when 1:N search returned a single item. Search & Verify flag should only be returned if 1:1
            // match occurred.
            return false
        }
        val matchResult = matchingStep.result as? MatchResult ?: return false
        // [MS-992] Returning 'searchAndVerifyMatched=true' only when 1:1 match was made and the result is positive
        return matchResult.results.size == 1
    }

    private fun updateMatcherStepPayload(
        currentStep: Step,
        result: Serializable,
    ) {
        if (currentStep.id == StepId.FACE_CAPTURE && result is FaceCaptureResult) {
            val matchingStep = steps.firstOrNull { it.id == StepId.FACE_MATCHER }

            if (matchingStep != null) {
                val faceSamples = result.results
                    .mapNotNull { it.sample }
                    .map { MatchParams.FaceSample(it.faceId, it.template) }
                val newPayload = matchingStep.payload
                    .getParcelable<MatchStepStubPayload>(MatchStepStubPayload.STUB_KEY)
                    ?.toFaceStepArgs(result.referenceId, faceSamples)

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
                } else {
                    val stepSdk = step.payload.getParcelable<MatchStepStubPayload>(MatchStepStubPayload.STUB_KEY)?.fingerprintSDK
                    stepSdk == captureParams?.fingerprintSDK
                }
            }

            if (matchingStep != null) {
                val fingerprintSamples = result.results
                    .mapNotNull { it.sample }
                    .map {
                        MatchParams.FingerprintSample(
                            fingerId = it.fingerIdentifier,
                            format = it.format,
                            template = it.template,
                        )
                    }
                val newPayload = matchingStep.payload
                    .getParcelable<MatchStepStubPayload>(MatchStepStubPayload.STUB_KEY)
                    ?.toFingerprintStepArgs(result.referenceId, fingerprintSamples)

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
