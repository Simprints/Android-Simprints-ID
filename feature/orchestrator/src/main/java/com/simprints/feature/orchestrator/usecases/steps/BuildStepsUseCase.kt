package com.simprints.feature.orchestrator.usecases.steps

import com.simprints.core.domain.common.FlowType
import com.simprints.face.capture.FaceCaptureContract
import com.simprints.feature.consent.ConsentContract
import com.simprints.feature.consent.ConsentType
import com.simprints.feature.enrollast.EnrolLastBiometricContract
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
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration.Modality
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.allowedAgeRanges
import com.simprints.infra.config.store.models.experimental
import com.simprints.infra.config.store.models.fromDomainToModuleApi
import com.simprints.infra.config.store.models.isAgeRestricted
import com.simprints.infra.config.store.models.sortedUniqueAgeGroups
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.matcher.MatchContract
import javax.inject.Inject

internal class BuildStepsUseCase @Inject constructor(
    private val buildMatcherSubjectQuery: BuildMatcherSubjectQueryUseCase,
    private val cache: OrchestratorCache,
    private val mapStepsForLastBiometrics: MapStepsForLastBiometricEnrolUseCase,
) {
    fun build(
        action: ActionRequest,
        projectConfiguration: ProjectConfiguration,
    ) = when (action) {
        is ActionRequest.EnrolActionRequest -> listOf(
            buildSetupStep(),
            buildAgeSelectionStepIfNeeded(action, projectConfiguration),
            buildConsentStepIfNeeded(ConsentType.ENROL, projectConfiguration),
            buildCaptureAndMatchStepsForEnrol(action, projectConfiguration),
        )

        is ActionRequest.IdentifyActionRequest -> {
            val subjectQuery = buildMatcherSubjectQuery(projectConfiguration, action)

            listOf(
                buildSetupStep(),
                buildValidateIdPoolStep(
                    subjectQuery = subjectQuery,
                    biometricDataSource = action.biometricDataSource,
                    callerPackageName = action.actionIdentifier.callerPackageName,
                    projectConfiguration = projectConfiguration,
                ),
                buildAgeSelectionStepIfNeeded(action, projectConfiguration),
                buildConsentStepIfNeeded(ConsentType.IDENTIFY, projectConfiguration),
                buildCaptureAndMatchStepsForIdentify(
                    action,
                    projectConfiguration,
                    subjectQuery = subjectQuery,
                ),
            )
        }

        is ActionRequest.VerifyActionRequest -> listOf(
            buildSetupStep(),
            buildAgeSelectionStepIfNeeded(action, projectConfiguration),
            buildFetchGuidStepIfNeeded(
                projectId = action.projectId,
                subjectId = action.verifyGuid,
                biometricDataSource = action.biometricDataSource,
                callerPackageName = action.actionIdentifier.callerPackageName,
            ),
            buildConsentStepIfNeeded(ConsentType.VERIFY, projectConfiguration),
            buildCaptureAndMatchStepsForVerify(action, projectConfiguration),
        )

        is ActionRequest.EnrolLastBiometricActionRequest -> listOf(
            buildEnrolLastBiometricStep(action, projectConfiguration),
        )

        is ActionRequest.ConfirmIdentityActionRequest -> listOf(
            buildConfirmIdentityStep(action),
        )
    }.flatten()

    fun buildCaptureAndMatchStepsForAgeGroup(
        action: ActionRequest,
        projectConfiguration: ProjectConfiguration,
        ageGroup: AgeGroup,
    ): List<Step> = when (action) {
        is ActionRequest.EnrolActionRequest -> buildCaptureAndMatchStepsForEnrol(
            action,
            projectConfiguration,
            ageGroup,
        )

        is ActionRequest.IdentifyActionRequest -> buildCaptureAndMatchStepsForIdentify(
            action,
            projectConfiguration,
            ageGroup,
            subjectQuery = buildMatcherSubjectQuery(projectConfiguration, action),
        )

        is ActionRequest.VerifyActionRequest -> buildCaptureAndMatchStepsForVerify(
            action,
            projectConfiguration,
            ageGroup,
        )

        else -> emptyList()
    }

    private fun buildCaptureAndMatchStepsForEnrol(
        action: ActionRequest.EnrolActionRequest,
        projectConfiguration: ProjectConfiguration,
        ageGroup: AgeGroup? = null,
    ): List<Step> {
        val resolvedAgeGroup = ageGroup ?: ageGroupFromSubjectAge(action, projectConfiguration)

        return listOf(
            buildCaptureSteps(
                projectConfiguration,
                FlowType.ENROL,
                resolvedAgeGroup,
            ),
            if (projectConfiguration.general.duplicateBiometricEnrolmentCheck) {
                buildMatcherSteps(
                    projectConfiguration,
                    FlowType.ENROL,
                    resolvedAgeGroup,
                    buildMatcherSubjectQuery(projectConfiguration, action),
                    BiometricDataSource.fromString(
                        action.biometricDataSource,
                        action.actionIdentifier.callerPackageName,
                    ),
                )
            } else {
                emptyList()
            },
        ).flatten()
    }

    private fun buildCaptureAndMatchStepsForIdentify(
        action: ActionRequest.IdentifyActionRequest,
        projectConfiguration: ProjectConfiguration,
        ageGroup: AgeGroup? = null,
        subjectQuery: SubjectQuery,
    ): List<Step> {
        val resolvedAgeGroup = ageGroup ?: ageGroupFromSubjectAge(action, projectConfiguration)

        return listOf(
            buildCaptureSteps(
                projectConfiguration,
                FlowType.IDENTIFY,
                resolvedAgeGroup,
            ),
            buildMatcherSteps(
                projectConfiguration,
                FlowType.IDENTIFY,
                resolvedAgeGroup,
                subjectQuery,
                BiometricDataSource.fromString(
                    action.biometricDataSource,
                    action.actionIdentifier.callerPackageName,
                ),
            ),
        ).flatten()
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
                SubjectQuery(subjectId = action.verifyGuid),
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
                params = FetchSubjectContract.getParams(projectId, subjectId),
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
        subjectQuery: SubjectQuery,
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
                    params = ValidateSubjectPoolContract.getParams(subjectQuery),
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
    ): List<Step> = when (modality) {
        Modality.FINGERPRINT -> {
            determineFingerprintSDKs(projectConfiguration, ageGroup).map { bioSDK ->

                val sdkConfiguration = projectConfiguration.fingerprint?.getSdkConfiguration(bioSDK)

                // TODO: fingersToCollect can be read directly from FingerprintCapture
                val fingersToCollect = sdkConfiguration
                    ?.fingersToCapture
                    .orEmpty()
                    .map { finger -> finger.fromDomainToModuleApi() }

                Step(
                    id = StepId.FINGERPRINT_CAPTURE,
                    navigationActionId = R.id.action_orchestratorFragment_to_fingerprintCapture,
                    destinationId = FingerprintCaptureContract.DESTINATION,
                    params = FingerprintCaptureContract.getParams(flowType, fingersToCollect, bioSDK),
                )
            }
        }

        Modality.FACE -> {
            determineFaceSDKs(projectConfiguration, ageGroup).map { bioSDK ->
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
        subjectQuery: SubjectQuery,
        biometricDataSource: BiometricDataSource,
    ): List<Step> = projectConfiguration.general.matchingModalities
        .flatMap { modality ->
            buildMatcherStepsForModality(modality, projectConfiguration, ageGroup, flowType, subjectQuery, biometricDataSource)
        }.takeIf { it.isNotEmpty() } ?: projectConfiguration.general.modalities.flatMap { modality ->
        buildMatcherStepsForModality(modality, projectConfiguration, ageGroup, flowType, subjectQuery, biometricDataSource)
    }

    private fun buildMatcherStepsForModality(
        modality: Modality,
        projectConfiguration: ProjectConfiguration,
        ageGroup: AgeGroup?,
        flowType: FlowType,
        subjectQuery: SubjectQuery,
        biometricDataSource: BiometricDataSource,
    ): List<Step> = when (modality) {
        Modality.FINGERPRINT -> {
            determineFingerprintSDKs(projectConfiguration, ageGroup).map { bioSDK ->
                Step(
                    id = StepId.FINGERPRINT_MATCHER,
                    navigationActionId = R.id.action_orchestratorFragment_to_matcher,
                    destinationId = MatchContract.DESTINATION,
                    params = MatchStepStubPayload.getMatchStubParams(
                        flowType = flowType,
                        subjectQuery = subjectQuery,
                        biometricDataSource = biometricDataSource,
                        fingerprintSDK = bioSDK,
                    ),
                )
            }
        }

        Modality.FACE -> {
            determineFaceSDKs(projectConfiguration, ageGroup).map { bioSDK ->
                // Face bio SDK is currently ignored until we add a second one
                Step(
                    id = StepId.FACE_MATCHER,
                    navigationActionId = R.id.action_orchestratorFragment_to_matcher,
                    destinationId = MatchContract.DESTINATION,
                    params = MatchStepStubPayload.getMatchStubParams(
                        flowType = flowType,
                        subjectQuery = subjectQuery,
                        biometricDataSource = biometricDataSource,
                        faceSDK = bioSDK,
                    ),
                )
            }
        }
    }

    private fun buildEnrolLastBiometricStep(
        action: ActionRequest.EnrolLastBiometricActionRequest,
        projectConfiguration: ProjectConfiguration,
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
                    ),
                )
        )
    }

    private fun buildConfirmIdentityStep(action: ActionRequest.ConfirmIdentityActionRequest) = listOf(
        Step(
            id = StepId.CONFIRM_IDENTITY,
            navigationActionId = R.id.action_orchestratorFragment_to_selectSubject,
            destinationId = SelectSubjectContract.DESTINATION,
            params = SelectSubjectContract.getParams(
                projectId = action.projectId,
                subjectId = action.selectedGuid,
            ),
        ),
    )

    private fun determineFingerprintSDKs(
        projectConfiguration: ProjectConfiguration,
        ageGroup: AgeGroup?,
    ): List<FingerprintConfiguration.BioSdk> {
        val sdks = mutableListOf<FingerprintConfiguration.BioSdk>()

        if (!projectConfiguration.isAgeRestricted()) {
            projectConfiguration.fingerprint?.allowedSDKs?.let { sdks.addAll(it) }
        } else {
            ageGroup?.let {
                if (projectConfiguration.fingerprint
                        ?.secugenSimMatcher
                        ?.allowedAgeRange
                        ?.contains(ageGroup) == true
                ) {
                    sdks.add(FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER)
                }
                if (projectConfiguration.fingerprint
                        ?.nec
                        ?.allowedAgeRange
                        ?.contains(ageGroup) == true
                ) {
                    sdks.add(FingerprintConfiguration.BioSdk.NEC)
                }
            }
        }

        return sdks
    }

    private fun determineFaceSDKs(
        projectConfiguration: ProjectConfiguration,
        ageGroup: AgeGroup?,
    ): List<FaceConfiguration.BioSdk> {
        val sdks = mutableListOf<FaceConfiguration.BioSdk>()

        if (!projectConfiguration.isAgeRestricted()) {
            projectConfiguration.face?.allowedSDKs?.let { sdks.addAll(it) }
        } else {
            ageGroup?.let {
                if (projectConfiguration.face
                        ?.rankOne
                        ?.allowedAgeRange
                        ?.contains(ageGroup) == true
                ) {
                    sdks.add(FaceConfiguration.BioSdk.RANK_ONE)
                }
                if (projectConfiguration.face
                        ?.simFace
                        ?.allowedAgeRange
                        ?.contains(ageGroup) == true
                ) {
                    sdks.add(FaceConfiguration.BioSdk.SIM_FACE)
                }
            }
        }

        return sdks
    }

    private fun ageGroupFromSubjectAge(
        action: ActionRequest,
        projectConfiguration: ProjectConfiguration,
    ): AgeGroup? = action.getSubjectAgeIfAvailable()?.let { subjectAge ->
        projectConfiguration.sortedUniqueAgeGroups().firstOrNull { it.includes(subjectAge) }
    }
}
