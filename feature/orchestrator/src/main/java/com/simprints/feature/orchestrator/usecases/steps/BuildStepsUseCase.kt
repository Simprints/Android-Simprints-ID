package com.simprints.feature.orchestrator.usecases.steps

import com.simprints.core.domain.common.AgeGroup
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.common.Modality
import com.simprints.face.capture.FaceCaptureContract
import com.simprints.feature.consent.ConsentContract
import com.simprints.feature.consent.ConsentType
import com.simprints.feature.enrollast.EnrolLastBiometricContract
import com.simprints.feature.externalcredential.ExternalCredentialContract
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.feature.fetchsubject.FetchSubjectContract
import com.simprints.feature.orchestrator.R
import com.simprints.feature.orchestrator.cache.OrchestratorCache
import com.simprints.feature.orchestrator.exceptions.SubjectAgeNotSupportedException
import com.simprints.feature.orchestrator.steps.MatchStepStubPayload
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.feature.orchestrator.steps.StepId
import com.simprints.feature.orchestrator.usecases.MapStepsForLastBiometricEnrolUseCase
import com.simprints.feature.selectagegroup.SelectSubjectAgeGroupContract
import com.simprints.feature.selectsubject.SelectSubjectContract
import com.simprints.feature.setup.SetupContract
import com.simprints.feature.validatepool.ValidateSubjectPoolContract
import com.simprints.fingerprint.capture.FingerprintCaptureContract
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.allowedAgeRanges
import com.simprints.infra.config.store.models.experimental
import com.simprints.infra.config.store.models.getSdkListForAgeGroup
import com.simprints.infra.config.store.models.isAgeRestricted
import com.simprints.infra.config.store.models.sortedUniqueAgeGroups
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.matcher.MatchContract
import javax.inject.Inject

internal class BuildStepsUseCase @Inject constructor(
    private val buildMatcherSubjectQuery: BuildMatcherSubjectQueryUseCase,
    private val cache: OrchestratorCache,
    private val mapStepsForLastBiometrics: MapStepsForLastBiometricEnrolUseCase,
    private val fallbackToCommCareDataSourceIfNeeded: FallbackToCommCareDataSourceIfNeededUseCase,
) {
    suspend fun build(
        action: ActionRequest,
        projectConfiguration: ProjectConfiguration,
        enrolmentSubjectId: String,
        cachedScannedCredential: ScannedCredential?,
    ) = when (action) {
        is ActionRequest.EnrolActionRequest -> {
            listOf(
                buildSetupStep(),
                buildAgeSelectionStepIfNeeded(action, projectConfiguration),
                buildConsentStepIfNeeded(ConsentType.ENROL, projectConfiguration),
                buildCaptureAndMatchStepsForEnrol(action, projectConfiguration, enrolmentSubjectId = enrolmentSubjectId),
            )
        }

        is ActionRequest.IdentifyActionRequest -> {
            val subjectQuery = buildMatcherSubjectQuery(projectConfiguration, action)

            listOf(
                buildSetupStep(),
                buildValidateIdPoolStep(
                    enrolmentRecordQuery = subjectQuery,
                    biometricDataSource = action.biometricDataSource,
                    callerPackageName = action.actionIdentifier.callerPackageName,
                    projectConfiguration = projectConfiguration,
                ),
                buildAgeSelectionStepIfNeeded(action, projectConfiguration),
                buildConsentStepIfNeeded(ConsentType.IDENTIFY, projectConfiguration),
                buildCaptureAndMatchStepsForIdentify(
                    action = action,
                    projectConfiguration = projectConfiguration,
                    enrolmentRecordQuery = subjectQuery,
                    enrolmentSubjectId = enrolmentSubjectId,
                ),
            )
        }

        is ActionRequest.VerifyActionRequest -> {
            listOf(
                buildSetupStep(),
                buildAgeSelectionStepIfNeeded(action, projectConfiguration),
                buildFetchGuidStepIfNeeded(
                    projectId = action.projectId,
                    subjectId = action.verifyGuid,
                    biometricDataSource = action.biometricDataSource,
                    callerPackageName = action.actionIdentifier.callerPackageName,
                    metadata = action.metadata,
                ),
                buildConsentStepIfNeeded(ConsentType.VERIFY, projectConfiguration),
                buildCaptureAndMatchStepsForVerify(action, projectConfiguration),
            )
        }

        is ActionRequest.EnrolLastBiometricActionRequest -> {
            listOf(
                buildEnrolLastBiometricStep(action, projectConfiguration, cachedScannedCredential),
            )
        }

        is ActionRequest.ConfirmIdentityActionRequest -> {
            listOf(
                buildConfirmIdentityStep(action, cachedScannedCredential),
            )
        }
    }.flatten()

    suspend fun buildCaptureAndMatchStepsForAgeGroup(
        action: ActionRequest,
        projectConfiguration: ProjectConfiguration,
        ageGroup: AgeGroup,
        enrolmentSubjectId: String,
    ): List<Step> = when (action) {
        is ActionRequest.EnrolActionRequest -> buildCaptureAndMatchStepsForEnrol(
            action,
            projectConfiguration,
            ageGroup,
            enrolmentSubjectId,
        )

        is ActionRequest.IdentifyActionRequest -> buildCaptureAndMatchStepsForIdentify(
            action = action,
            projectConfiguration = projectConfiguration,
            ageGroup = ageGroup,
            enrolmentRecordQuery = buildMatcherSubjectQuery(projectConfiguration, action),
            enrolmentSubjectId = enrolmentSubjectId,
        )

        is ActionRequest.VerifyActionRequest -> buildCaptureAndMatchStepsForVerify(
            action,
            projectConfiguration,
            ageGroup,
        )

        else -> emptyList()
    }

    private fun buildExternalCredentialStepIfNeeded(
        ageGroup: AgeGroup?,
        enrolmentSubjectId: String,
        projectConfiguration: ProjectConfiguration,
        flowType: FlowType,
    ): List<Step> {
        val isExternalCredentialEnabled = projectConfiguration.multifactorId?.allowedExternalCredentials?.isNotEmpty() ?: false
        if (!isExternalCredentialEnabled) return emptyList()

        return when (flowType) {
            FlowType.ENROL, FlowType.IDENTIFY -> {
                listOf(
                    Step(
                        id = StepId.EXTERNAL_CREDENTIAL,
                        navigationActionId = R.id.action_orchestratorFragment_to_externalCredential,
                        destinationId = ExternalCredentialContract.DESTINATION,
                        params = ExternalCredentialContract.getParams(
                            subjectId = enrolmentSubjectId,
                            flowType = flowType,
                            ageGroup = ageGroup,
                        ),
                    ),
                )
            }

            FlowType.VERIFY -> {
                emptyList()
            }
        }
    }

    private suspend fun buildCaptureAndMatchStepsForEnrol(
        action: ActionRequest.EnrolActionRequest,
        projectConfiguration: ProjectConfiguration,
        ageGroup: AgeGroup? = null,
        enrolmentSubjectId: String,
    ): List<Step> {
        val action = fallbackToCommCareDataSourceIfNeeded(action, projectConfiguration)
        val resolvedAgeGroup = ageGroup ?: ageGroupFromSubjectAge(action, projectConfiguration)
        val enrolFlowType = FlowType.ENROL
        val captureSteps = buildCaptureSteps(
            projectConfiguration,
            enrolFlowType,
            resolvedAgeGroup,
        )
        val externalCredentialStep = when {
            captureSteps.isEmpty() -> emptyList()

            else -> buildExternalCredentialStepIfNeeded(
                ageGroup = ageGroup,
                enrolmentSubjectId = enrolmentSubjectId,
                projectConfiguration = projectConfiguration,
                flowType = enrolFlowType,
            )
        }
        val matcherSteps = if (projectConfiguration.general.duplicateBiometricEnrolmentCheck) {
            buildMatcherSteps(
                projectConfiguration,
                enrolFlowType,
                resolvedAgeGroup,
                buildMatcherSubjectQuery(projectConfiguration, action),
                BiometricDataSource.fromString(
                    action.biometricDataSource,
                    action.actionIdentifier.callerPackageName,
                ),
            )
        } else {
            emptyList()
        }
        return captureSteps + externalCredentialStep + matcherSteps
    }

    private suspend fun buildCaptureAndMatchStepsForIdentify(
        action: ActionRequest.IdentifyActionRequest,
        projectConfiguration: ProjectConfiguration,
        ageGroup: AgeGroup? = null,
        enrolmentRecordQuery: EnrolmentRecordQuery,
        enrolmentSubjectId: String,
    ): List<Step> {
        val action = fallbackToCommCareDataSourceIfNeeded(action, projectConfiguration)
        val resolvedAgeGroup = ageGroup ?: ageGroupFromSubjectAge(action, projectConfiguration)
        val identifyFlowType = FlowType.IDENTIFY
        val captureSteps = buildCaptureSteps(
            projectConfiguration,
            identifyFlowType,
            resolvedAgeGroup,
        )
        val externalCredentialStep = when {
            captureSteps.isEmpty() -> emptyList()

            else -> buildExternalCredentialStepIfNeeded(
                ageGroup = ageGroup,
                enrolmentSubjectId = enrolmentSubjectId,
                projectConfiguration = projectConfiguration,
                flowType = identifyFlowType,
            )
        }
        val matcherSteps = buildMatcherSteps(
            projectConfiguration,
            identifyFlowType,
            resolvedAgeGroup,
            enrolmentRecordQuery,
            BiometricDataSource.fromString(
                action.biometricDataSource,
                action.actionIdentifier.callerPackageName,
            ),
        )
        return captureSteps + externalCredentialStep + matcherSteps
    }

    private fun buildCaptureAndMatchStepsForVerify(
        action: ActionRequest.VerifyActionRequest,
        projectConfiguration: ProjectConfiguration,
        ageGroup: AgeGroup? = null,
    ): List<Step> {
        val resolvedAgeGroup = ageGroup ?: ageGroupFromSubjectAge(action, projectConfiguration)

        return listOf(
            buildCaptureSteps(
                projectConfiguration,
                FlowType.VERIFY,
                resolvedAgeGroup,
            ),
            buildMatcherSteps(
                projectConfiguration,
                FlowType.VERIFY,
                resolvedAgeGroup,
                EnrolmentRecordQuery(subjectId = action.verifyGuid, metadata = action.metadata),
                BiometricDataSource.fromString(
                    action.biometricDataSource,
                    action.actionIdentifier.callerPackageName,
                ),
            ),
        ).flatten()
    }

    private fun buildAgeSelectionStepIfNeeded(
        action: ActionRequest,
        projectConfiguration: ProjectConfiguration,
    ): List<Step> {
        if (projectConfiguration.isAgeRestricted()) {
            val subjectAge = action.getSubjectAgeIfAvailable()
            if (subjectAge == null) {
                return listOf(
                    Step(
                        id = StepId.SELECT_SUBJECT_AGE,
                        navigationActionId = R.id.action_orchestratorFragment_to_age_group_selection,
                        destinationId = SelectSubjectAgeGroupContract.DESTINATION,
                    ),
                )
            } else if (projectConfiguration.allowedAgeRanges().none { it.includes(subjectAge) }) {
                throw SubjectAgeNotSupportedException()
            }
        }

        return emptyList()
    }

    private fun buildSetupStep() = listOf(
        Step(
            id = StepId.SETUP,
            navigationActionId = R.id.action_orchestratorFragment_to_setup,
            destinationId = SetupContract.DESTINATION,
        ),
    )

    private fun buildFetchGuidStepIfNeeded(
        projectId: String,
        subjectId: String,
        biometricDataSource: String,
        callerPackageName: String,
        metadata: String,
    ) = when (
        BiometricDataSource.fromString(
            value = biometricDataSource,
            callerPackageName = callerPackageName,
        )
    ) {
        BiometricDataSource.Simprints -> listOf(
            Step(
                id = StepId.FETCH_GUID,
                navigationActionId = R.id.action_orchestratorFragment_to_fetchSubject,
                destinationId = FetchSubjectContract.DESTINATION,
                params = FetchSubjectContract.getParams(projectId, subjectId, metadata),
            ),
        )

        is BiometricDataSource.CommCare -> emptyList()
    }

    private fun buildConsentStepIfNeeded(
        consentType: ConsentType,
        projectConfiguration: ProjectConfiguration,
    ) = if (projectConfiguration.consent.collectConsent) {
        listOf(
            Step(
                id = StepId.CONSENT,
                navigationActionId = R.id.action_orchestratorFragment_to_consent,
                destinationId = ConsentContract.DESTINATION,
                params = ConsentContract.getParams(consentType),
            ),
        )
    } else {
        emptyList()
    }

    private fun buildValidateIdPoolStep(
        enrolmentRecordQuery: EnrolmentRecordQuery,
        biometricDataSource: String,
        callerPackageName: String,
        projectConfiguration: ProjectConfiguration,
    ) = if (projectConfiguration.experimental().idPoolValidationEnabled) {
        when (
            BiometricDataSource.fromString(
                value = biometricDataSource,
                callerPackageName = callerPackageName,
            )
        ) {
            BiometricDataSource.Simprints -> listOf(
                Step(
                    id = StepId.VALIDATE_ID_POOL,
                    navigationActionId = R.id.action_orchestratorFragment_to_validateSubjectPool,
                    destinationId = ValidateSubjectPoolContract.DESTINATION,
                    params = ValidateSubjectPoolContract.getParams(enrolmentRecordQuery),
                ),
            )

            is BiometricDataSource.CommCare -> emptyList()
        }
    } else {
        emptyList()
    }

    /**
     * Builds the capture steps for the given flow type and age group.
     *
     * If none of the capturing modalities are available for the age group,
     * the function will fall back to all configured modalities.
     */
    private fun buildCaptureSteps(
        projectConfiguration: ProjectConfiguration,
        flowType: FlowType,
        ageGroup: AgeGroup?,
    ): List<Step> {
        // Cache the age group used for capture in case it's needed for Enrol Last followup
        cache.ageGroup = ageGroup

        return capturingModalitiesForFlowType(projectConfiguration, flowType)
            .flatMap { modality ->
                buildCaptureStepsForModality(modality, projectConfiguration, ageGroup, flowType)
            }.takeIf { it.isNotEmpty() }
            ?: projectConfiguration.general.modalities.flatMap { modality ->
                buildCaptureStepsForModality(modality, projectConfiguration, ageGroup, flowType)
            }
    }

    /**
     * When enrolling capture all configured modalities.
     * When identifying or verifying, capture only the modalities that will be used for matching.
     */
    private fun capturingModalitiesForFlowType(
        projectConfiguration: ProjectConfiguration,
        flowType: FlowType,
    ): List<Modality> = when (flowType) {
        FlowType.ENROL -> projectConfiguration.general.modalities
        FlowType.IDENTIFY -> projectConfiguration.general.matchingModalities
        FlowType.VERIFY -> projectConfiguration.general.matchingModalities
    }

    private fun buildCaptureStepsForModality(
        modality: Modality,
        projectConfiguration: ProjectConfiguration,
        ageGroup: AgeGroup?,
        flowType: FlowType,
    ): List<Step> = projectConfiguration.getSdkListForAgeGroup(modality, ageGroup).map { bioSDK ->
        when (bioSDK.modality()) {
            Modality.FINGERPRINT -> {
                val sdkConfiguration = projectConfiguration.fingerprint?.getSdkConfiguration(bioSDK)

                // TODO: fingersToCollect can be read directly from FingerprintCapture
                val fingersToCollect = sdkConfiguration
                    ?.fingersToCapture
                    .orEmpty()

                Step(
                    id = StepId.FINGERPRINT_CAPTURE,
                    navigationActionId = R.id.action_orchestratorFragment_to_fingerprintCapture,
                    destinationId = FingerprintCaptureContract.DESTINATION,
                    params = FingerprintCaptureContract.getParams(flowType, fingersToCollect, bioSDK),
                )
            }

            Modality.FACE -> {
                val sdkConfiguration = projectConfiguration.face?.getSdkConfiguration(bioSDK)

                // TODO: samplesToCapture can be read directly from FaceCapture
                val samplesToCapture = sdkConfiguration?.nbOfImagesToCapture ?: 0
                Step(
                    id = StepId.FACE_CAPTURE,
                    navigationActionId = R.id.action_orchestratorFragment_to_faceCapture,
                    destinationId = FaceCaptureContract.DESTINATION,
                    params = FaceCaptureContract.getParams(samplesToCapture, bioSDK),
                )
            }

        }
    }

    /**
     * Builds the matcher steps for the given flow type, age group, subject query and biometric data source.
     *
     * If none of the matching modalities are available for the age group,
     * the function will fall back to all configured modalities.
     */
    private fun buildMatcherSteps(
        projectConfiguration: ProjectConfiguration,
        flowType: FlowType,
        ageGroup: AgeGroup?,
        enrolmentRecordQuery: EnrolmentRecordQuery,
        biometricDataSource: BiometricDataSource,
    ): List<Step> = projectConfiguration.general.matchingModalities
        .flatMap { modality ->
            buildMatcherStepsForModality(modality, projectConfiguration, ageGroup, flowType, enrolmentRecordQuery, biometricDataSource)
        }.ifEmpty {
            projectConfiguration.general.modalities.flatMap { modality ->
                buildMatcherStepsForModality(modality, projectConfiguration, ageGroup, flowType, enrolmentRecordQuery, biometricDataSource)
            }
        }

    private fun buildMatcherStepsForModality(
        modality: Modality,
        projectConfiguration: ProjectConfiguration,
        ageGroup: AgeGroup?,
        flowType: FlowType,
        enrolmentRecordQuery: EnrolmentRecordQuery,
        biometricDataSource: BiometricDataSource,
    ): List<Step> = projectConfiguration.getSdkListForAgeGroup(modality, ageGroup).mapNotNull { bioSDK ->
        val paramStub = MatchStepStubPayload.getMatchStubParams(
            flowType = flowType,
            enrolmentRecordQuery = enrolmentRecordQuery,
            biometricDataSource = biometricDataSource,
            bioSdk = bioSDK,
        )

        when (bioSDK.modality()){
            Modality.FINGERPRINT -> Step(
                id = StepId.FINGERPRINT_MATCHER,
                navigationActionId = R.id.action_orchestratorFragment_to_matcher,
                destinationId = MatchContract.DESTINATION,
                params = paramStub,
            )

            Modality.FACE -> Step(
                id = StepId.FACE_MATCHER,
                navigationActionId = R.id.action_orchestratorFragment_to_matcher,
                destinationId = MatchContract.DESTINATION,
                params = paramStub,
            )

            else -> null
        }
    }

    private fun buildEnrolLastBiometricStep(
        action: ActionRequest.EnrolLastBiometricActionRequest,
        projectConfiguration: ProjectConfiguration,
        cachedScannedCredential: ScannedCredential?,
    ): List<Step> {
        // Get capture steps needed for enrolment
        val enrolCaptureSteps = buildCaptureSteps(
            projectConfiguration,
            FlowType.ENROL,
            cache.ageGroup,
        )

        // Get a list of the enrolment capture steps that have not been completed yet
        // e.g. because they were skipped due to matching modalities configuration
        val missingCaptureSteps = enrolCaptureSteps.filter { enrolCaptureStep ->
            cache.steps.none { it.id == enrolCaptureStep.id }
        }

        return (
            missingCaptureSteps +
                Step(
                    id = StepId.ENROL_LAST_BIOMETRIC,
                    navigationActionId = R.id.action_orchestratorFragment_to_enrolLast,
                    destinationId = EnrolLastBiometricContract.DESTINATION,
                    params = EnrolLastBiometricContract.getParams(
                        projectId = action.projectId,
                        userId = action.userId,
                        moduleId = action.moduleId,
                        steps = mapStepsForLastBiometrics(cache.steps.mapNotNull { it.result }),
                        scannedCredential = cachedScannedCredential,
                    ),
                )
        )
    }

    private fun buildConfirmIdentityStep(
        action: ActionRequest.ConfirmIdentityActionRequest,
        cachedScannedCredential: ScannedCredential?,
    ) = listOf(
        Step(
            id = StepId.CONFIRM_IDENTITY,
            navigationActionId = R.id.action_orchestratorFragment_to_selectSubject,
            destinationId = SelectSubjectContract.DESTINATION,
            params = SelectSubjectContract.getParams(
                projectId = action.projectId,
                subjectId = action.selectedGuid,
                scannedCredential = cachedScannedCredential,
            ),
        ),
    )

    private fun ageGroupFromSubjectAge(
        action: ActionRequest,
        projectConfiguration: ProjectConfiguration,
    ): AgeGroup? = action.getSubjectAgeIfAvailable()?.let { subjectAge ->
        projectConfiguration.sortedUniqueAgeGroups().firstOrNull { it.includes(subjectAge) }
    }
}
