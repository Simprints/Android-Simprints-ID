package com.simprints.feature.orchestrator.usecases.steps

import androidx.core.os.bundleOf
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
import com.simprints.infra.config.store.models.fromDomainToModuleApi
import com.simprints.infra.config.store.models.isAgeRestricted
import com.simprints.infra.config.store.models.sortedUniqueAgeGroups
import com.simprints.infra.enrolment.records.store.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.matcher.MatchContract
import javax.inject.Inject

internal class BuildStepsUseCase @Inject constructor(
    private val buildMatcherSubjectQuery: BuildMatcherSubjectQueryUseCase,
    private val cache: OrchestratorCache,
    private val mapStepsForLastBiometrics: MapStepsForLastBiometricEnrolUseCase,
) {

    fun build(action: ActionRequest, projectConfiguration: ProjectConfiguration) = when (action) {
        is ActionRequest.EnrolActionRequest -> listOf(
            buildSetupStep(),
            buildAgeSelectionStepIfNeeded(action, projectConfiguration),
            buildConsentStepIfNeeded(ConsentType.ENROL, projectConfiguration),
            buildModalityCaptureAndMatchStepsForEnrol(action, projectConfiguration)
        )

        is ActionRequest.IdentifyActionRequest -> {
            val subjectQuery = buildMatcherSubjectQuery(projectConfiguration, action)

            listOf(
                buildSetupStep(),
                buildValidateIdPoolStep(
                    subjectQuery = subjectQuery,
                    biometricDataSource = action.biometricDataSource,
                    callerPackageName = action.callerPackageName
                ),
                buildAgeSelectionStepIfNeeded(action, projectConfiguration),
                buildConsentStepIfNeeded(ConsentType.IDENTIFY, projectConfiguration),
                buildModalityCaptureAndMatchStepsForIdentify(
                    action,
                    projectConfiguration,
                    subjectQuery = subjectQuery,
                )
            )
        }

        is ActionRequest.VerifyActionRequest -> listOf(
            buildSetupStep(),
            buildAgeSelectionStepIfNeeded(action, projectConfiguration),
            buildFetchGuidStepIfNeeded(
                projectId = action.projectId,
                subjectId = action.verifyGuid,
                biometricDataSource = action.biometricDataSource,
                callerPackageName = action.callerPackageName
            ),
            buildConsentStepIfNeeded(ConsentType.VERIFY, projectConfiguration),
            buildModalityCaptureAndMatchStepsForVerify(action, projectConfiguration)
        )

        is ActionRequest.EnrolLastBiometricActionRequest -> listOf(
            buildEnrolLastBiometricStep(action),
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
        is ActionRequest.EnrolActionRequest -> buildModalityCaptureAndMatchStepsForEnrol(
            action,
            projectConfiguration,
            ageGroup,
        )

        is ActionRequest.IdentifyActionRequest -> buildModalityCaptureAndMatchStepsForIdentify(
            action,
            projectConfiguration,
            ageGroup,
            subjectQuery = buildMatcherSubjectQuery(projectConfiguration, action),
        )

        is ActionRequest.VerifyActionRequest -> buildModalityCaptureAndMatchStepsForVerify(
            action,
            projectConfiguration,
            ageGroup,
        )

        else -> emptyList()
    }

    private fun buildModalityCaptureAndMatchStepsForEnrol(
        action: ActionRequest.EnrolActionRequest,
        projectConfiguration: ProjectConfiguration,
        ageGroup: AgeGroup? = null,
    ): List<Step> {
        val resolvedAgeGroup = ageGroup ?: ageGroupFromSubjectAge(action, projectConfiguration)

        return listOf(
            buildModalityCaptureSteps(
                projectConfiguration,
                FlowType.ENROL,
                resolvedAgeGroup,
            ),
            if (projectConfiguration.general.duplicateBiometricEnrolmentCheck) {
                buildModalityMatcherSteps(
                    projectConfiguration,
                    FlowType.ENROL,
                    resolvedAgeGroup,
                    buildMatcherSubjectQuery(projectConfiguration, action),
                    BiometricDataSource.fromString(
                        action.biometricDataSource,
                        action.callerPackageName
                    ),
                )
            } else emptyList(),
        ).flatten()
    }

    private fun buildModalityCaptureAndMatchStepsForIdentify(
        action: ActionRequest.IdentifyActionRequest,
        projectConfiguration: ProjectConfiguration,
        ageGroup: AgeGroup? = null,
        subjectQuery: SubjectQuery,
    ): List<Step> {
        val resolvedAgeGroup = ageGroup ?: ageGroupFromSubjectAge(action, projectConfiguration)

        return listOf(
            buildModalityCaptureSteps(
                projectConfiguration,
                FlowType.IDENTIFY,
                resolvedAgeGroup,
            ),
            buildModalityMatcherSteps(
                projectConfiguration,
                FlowType.IDENTIFY,
                resolvedAgeGroup,
                subjectQuery,
                BiometricDataSource.fromString(
                    action.biometricDataSource,
                    action.callerPackageName
                ),
            )
        ).flatten()
    }

    private fun buildModalityCaptureAndMatchStepsForVerify(
        action: ActionRequest.VerifyActionRequest,
        projectConfiguration: ProjectConfiguration,
        ageGroup: AgeGroup? = null,
    ): List<Step> {
        val resolvedAgeGroup = ageGroup ?: ageGroupFromSubjectAge(action, projectConfiguration)

        return listOf(
            buildModalityCaptureSteps(
                projectConfiguration,
                FlowType.VERIFY,
                resolvedAgeGroup,
            ),
            buildModalityMatcherSteps(
                projectConfiguration,
                FlowType.VERIFY,
                resolvedAgeGroup,
                SubjectQuery(subjectId = action.verifyGuid),
                BiometricDataSource.fromString(
                    action.biometricDataSource,
                    action.callerPackageName
                ),
            )
        ).flatten()
    }

    private fun buildAgeSelectionStepIfNeeded(
        action: ActionRequest,
        projectConfiguration: ProjectConfiguration
    ): List<Step> {
        if (projectConfiguration.isAgeRestricted()) {
            val subjectAge = action.getSubjectAgeIfAvailable()
            if (subjectAge == null) {
                return listOf(
                    Step(
                        id = StepId.SELECT_SUBJECT_AGE,
                        navigationActionId = R.id.action_orchestratorFragment_to_age_group_selection,
                        destinationId = SelectSubjectAgeGroupContract.DESTINATION,
                        payload = bundleOf()
                    )
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
            payload = bundleOf(),
        )
    )

    private fun buildFetchGuidStepIfNeeded(
        projectId: String,
        subjectId: String,
        biometricDataSource: String,
        callerPackageName: String
    ) = when (BiometricDataSource.fromString(
        value = biometricDataSource,
        callerPackageName = callerPackageName
    )) {
        BiometricDataSource.Simprints -> listOf(
            Step(
                id = StepId.FETCH_GUID,
                navigationActionId = R.id.action_orchestratorFragment_to_fetchSubject,
                destinationId = FetchSubjectContract.DESTINATION,
                payload = FetchSubjectContract.getArgs(projectId, subjectId),
            )
        )

        is BiometricDataSource.CommCare -> emptyList()
    }

    private fun buildConsentStepIfNeeded(
        consentType: ConsentType,
        projectConfiguration: ProjectConfiguration,
    ) = if (projectConfiguration.consent.collectConsent) listOf(
        Step(
            id = StepId.CONSENT,
            navigationActionId = R.id.action_orchestratorFragment_to_consent,
            destinationId = ConsentContract.DESTINATION,
            payload = ConsentContract.getArgs(consentType),
        )
    ) else emptyList()

    private fun buildValidateIdPoolStep(
        subjectQuery: SubjectQuery,
        biometricDataSource: String,
        callerPackageName: String
    ) = when (BiometricDataSource.fromString(
        value = biometricDataSource,
        callerPackageName = callerPackageName
    )) {
        BiometricDataSource.Simprints -> listOf(
            Step(
                id = StepId.VALIDATE_ID_POOL,
                navigationActionId = R.id.action_orchestratorFragment_to_validateSubjectPool,
                destinationId = ValidateSubjectPoolContract.DESTINATION,
                payload = ValidateSubjectPoolContract.getArgs(subjectQuery),
            )
        )

        is BiometricDataSource.CommCare -> emptyList()
    }

    private fun buildModalityCaptureSteps(
        projectConfiguration: ProjectConfiguration,
        flowType: FlowType,
        ageGroup: AgeGroup?,
    ): List<Step> = projectConfiguration.general.modalities.flatMap { modality ->
        when (modality) {
            Modality.FINGERPRINT -> {
                determineFingerprintSDKs(projectConfiguration, ageGroup).map { bioSDK ->

                    val sdkConfiguration = projectConfiguration.fingerprint?.getSdkConfiguration(bioSDK)

                    //TODO: fingersToCollect can be read directly from FingerprintCapture
                    val fingersToCollect = sdkConfiguration?.fingersToCapture.orEmpty()
                        .map { finger -> finger.fromDomainToModuleApi() }

                    Step(
                        id = StepId.FINGERPRINT_CAPTURE,
                        navigationActionId = R.id.action_orchestratorFragment_to_fingerprintCapture,
                        destinationId = FingerprintCaptureContract.DESTINATION,
                        payload = FingerprintCaptureContract.getArgs(flowType, fingersToCollect, bioSDK)
                    )
                }
            }

            Modality.FACE -> {
                determineFaceSDKs(projectConfiguration, ageGroup).map {
                    // Face bio SDK is currently ignored until we add a second one
                    //TODO: samplesToCapture can be read directly from FaceCapture
                    val samplesToCapture = projectConfiguration.face?.nbOfImagesToCapture ?: 0
                    Step(
                        id = StepId.FACE_CAPTURE,
                        navigationActionId = R.id.action_orchestratorFragment_to_faceCapture,
                        destinationId = FaceCaptureContract.DESTINATION,
                        payload = FaceCaptureContract.getArgs(samplesToCapture),
                    )
                }
            }
        }
    }

    private fun buildModalityMatcherSteps(
        projectConfiguration: ProjectConfiguration,
        flowType: FlowType,
        ageGroup: AgeGroup?,
        subjectQuery: SubjectQuery,
        biometricDataSource: BiometricDataSource,
    ): List<Step> = projectConfiguration.general.modalities.flatMap { modality ->
        when (modality) {
            Modality.FINGERPRINT -> {
                determineFingerprintSDKs(projectConfiguration, ageGroup).map { bioSDK ->
                    Step(
                        id = StepId.FINGERPRINT_MATCHER,
                        navigationActionId = R.id.action_orchestratorFragment_to_matcher,
                        destinationId = MatchContract.DESTINATION,
                        payload = MatchStepStubPayload.asBundle(flowType, subjectQuery, biometricDataSource, bioSDK),
                    )
                }
            }

            Modality.FACE -> {
                determineFaceSDKs(projectConfiguration, ageGroup).map {
                    // Face bio SDK is currently ignored until we add a second one
                    Step(
                        id = StepId.FACE_MATCHER,
                        navigationActionId = R.id.action_orchestratorFragment_to_matcher,
                        destinationId = MatchContract.DESTINATION,
                        payload = MatchStepStubPayload.asBundle(flowType, subjectQuery, biometricDataSource),
                    )
                }
            }
        }
    }

    private fun buildEnrolLastBiometricStep(action: ActionRequest.EnrolLastBiometricActionRequest) =
        listOf(
            Step(
                id = StepId.ENROL_LAST_BIOMETRIC,
                navigationActionId = R.id.action_orchestratorFragment_to_enrolLast,
                destinationId = EnrolLastBiometricContract.DESTINATION,
                payload = EnrolLastBiometricContract.getArgs(
                    projectId = action.projectId,
                    userId = action.userId,
                    moduleId = action.moduleId,
                    steps = mapStepsForLastBiometrics(cache.steps.mapNotNull { it.result }),
                ),
            )
        )

    private fun buildConfirmIdentityStep(action: ActionRequest.ConfirmIdentityActionRequest) =
        listOf(
            Step(
                id = StepId.CONFIRM_IDENTITY,
                navigationActionId = R.id.action_orchestratorFragment_to_selectSubject,
                destinationId = SelectSubjectContract.DESTINATION,
                payload = SelectSubjectContract.getArgs(
                    projectId = action.projectId,
                    subjectId = action.selectedGuid,
                ),
            )
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
                if (projectConfiguration.fingerprint?.secugenSimMatcher?.allowedAgeRange?.contains(ageGroup) == true) {
                    sdks.add(FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER)
                }
                if (projectConfiguration.fingerprint?.nec?.allowedAgeRange?.contains(ageGroup) == true) {
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
                if (projectConfiguration.face?.rankOne?.allowedAgeRange?.contains(ageGroup) == true) {
                    sdks.add(FaceConfiguration.BioSdk.RANK_ONE)
                }
            }
        }

        return sdks
    }

    private fun ageGroupFromSubjectAge(action: ActionRequest, projectConfiguration: ProjectConfiguration): AgeGroup? {
        return action.getSubjectAgeIfAvailable()?.let { subjectAge ->
            projectConfiguration.sortedUniqueAgeGroups().firstOrNull { it.includes(subjectAge) }
        }
    }
}
