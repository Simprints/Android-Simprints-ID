package com.simprints.feature.orchestrator.usecases.steps

import androidx.core.os.bundleOf
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.common.FlowType
import com.simprints.face.capture.FaceCaptureContract
import com.simprints.feature.consent.ConsentContract
import com.simprints.feature.consent.ConsentType
import com.simprints.feature.enrollast.EnrolLastBiometricContract
import com.simprints.feature.fetchsubject.FetchSubjectContract
import com.simprints.feature.orchestrator.R
import com.simprints.feature.orchestrator.cache.OrchestratorCache
import com.simprints.feature.orchestrator.steps.MatchStepStubPayload
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.feature.orchestrator.steps.StepId
import com.simprints.feature.orchestrator.usecases.MapStepsForLastBiometricEnrolUseCase
import com.simprints.feature.selectagegroup.SelectSubjectAgeGroupContract
import com.simprints.feature.selectsubject.SelectSubjectContract
import com.simprints.feature.setup.SetupContract
import com.simprints.feature.validatepool.ValidateSubjectPoolContract
import com.simprints.fingerprint.capture.FingerprintCaptureContract
import com.simprints.infra.config.store.models.GeneralConfiguration.Modality
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.allowedAgeRanges
import com.simprints.infra.config.store.models.fromDomainToModuleApi
import com.simprints.infra.enrolment.records.store.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.matcher.MatchContract
import javax.inject.Inject


@ExcludedFromGeneratedTestCoverageReports("Mapping code for steps")
internal class BuildStepsUseCase @Inject constructor(
    private val buildMatcherSubjectQuery: BuildMatcherSubjectQueryUseCase,
    private val cache: OrchestratorCache,
    private val mapStepsForLastBiometrics: MapStepsForLastBiometricEnrolUseCase,
) {

    fun build(action: ActionRequest, projectConfiguration: ProjectConfiguration) = when (action) {
        is ActionRequest.EnrolActionRequest -> listOf(
            buildSetupStep(),
            buildConsentStep(ConsentType.ENROL),
            buildAgeSelectionStep(action, projectConfiguration),
            buildModalityCaptureSteps(
                projectConfiguration,
                FlowType.ENROL,
            ),
            if (projectConfiguration.general.duplicateBiometricEnrolmentCheck) {
                buildModalityMatcherSteps(
                    projectConfiguration,
                    FlowType.ENROL,
                    buildMatcherSubjectQuery(projectConfiguration, action),
                    BiometricDataSource.fromString(action.biometricDataSource),
                )
            } else emptyList(),
        )

        is ActionRequest.IdentifyActionRequest -> {
            val subjectQuery = buildMatcherSubjectQuery(projectConfiguration, action)

            listOf(
                buildSetupStep(),
                buildValidateIdPoolStep(subjectQuery),
                buildAgeSelectionStep(action, projectConfiguration),
                buildConsentStep(ConsentType.IDENTIFY),
                buildModalityCaptureSteps(
                    projectConfiguration,
                    FlowType.IDENTIFY,
                ),
                buildModalityMatcherSteps(
                    projectConfiguration,
                    FlowType.IDENTIFY,
                    subjectQuery,
                    BiometricDataSource.fromString(action.biometricDataSource),
                )
            )
        }

        is ActionRequest.VerifyActionRequest -> listOf(
            buildSetupStep(),
            buildAgeSelectionStep(action, projectConfiguration),
            buildFetchGuidStep(action.projectId, action.verifyGuid),
            buildConsentStep(ConsentType.VERIFY),
            buildModalityCaptureSteps(
                projectConfiguration,
                FlowType.VERIFY,
            ),
            buildModalityMatcherSteps(
                projectConfiguration,
                FlowType.VERIFY,
                SubjectQuery(subjectId = action.verifyGuid),
                BiometricDataSource.fromString(action.biometricDataSource),
            )
        )

        is ActionRequest.EnrolLastBiometricActionRequest -> listOf(
            buildEnrolLastBiometricStep(action),
        )

        is ActionRequest.ConfirmIdentityActionRequest -> listOf(
            buildConfirmIdentityStep(action),
        )
    }.flatten()

    private fun buildAgeSelectionStep(
        action: ActionRequest,
        projectConfiguration: ProjectConfiguration
    ): List<Step> {
        if (projectConfiguration.allowedAgeRanges().isEmpty()) {
            return emptyList()
        }
        // Todo check if the action request contains the age parameter
        return listOf(
            Step(
                id = StepId.SELECT_SUBJECT_AGE,
                navigationActionId = R.id.action_orchestratorFragment_to_age_group_selection,
                destinationId = SelectSubjectAgeGroupContract.DESTINATION,
                payload = bundleOf()
            )
        )

    }

    private fun buildSetupStep() = listOf(
        Step(
            id = StepId.SETUP,
            navigationActionId = R.id.action_orchestratorFragment_to_setup,
            destinationId = SetupContract.DESTINATION,
            payload = bundleOf(),
        )
    )

    private fun buildFetchGuidStep(projectId: String, subjectId: String) = listOf(
        Step(
            id = StepId.FETCH_GUID,
            navigationActionId = R.id.action_orchestratorFragment_to_fetchSubject,
            destinationId = FetchSubjectContract.DESTINATION,
            payload = FetchSubjectContract.getArgs(projectId, subjectId),
        )
    )

    private fun buildConsentStep(consentType: ConsentType) = listOf(
        Step(
            id = StepId.CONSENT,
            navigationActionId = R.id.action_orchestratorFragment_to_consent,
            destinationId = ConsentContract.DESTINATION,
            payload = ConsentContract.getArgs(consentType),
        )
    )

    private fun buildValidateIdPoolStep(subjectQuery: SubjectQuery) = listOf(Step(
        id = StepId.VALIDATE_ID_POOL,
        navigationActionId = R.id.action_orchestratorFragment_to_validateSubjectPool,
        destinationId = ValidateSubjectPoolContract.DESTINATION,
        payload = ValidateSubjectPoolContract.getArgs(subjectQuery),
    ))

    private fun buildModalityCaptureSteps(
        projectConfiguration: ProjectConfiguration,
        flowType: FlowType,
    ) = projectConfiguration.general.modalities.map {
        when (it) {
            Modality.FINGERPRINT -> {
                val fingersToCollect =
                    projectConfiguration.fingerprint?.bioSdkConfiguration?.fingersToCapture.orEmpty()
                        .map { finger -> finger.fromDomainToModuleApi() }

                Step(
                    id = StepId.FINGERPRINT_CAPTURE,
                    navigationActionId = R.id.action_orchestratorFragment_to_fingerprintCapture,
                    destinationId = FingerprintCaptureContract.DESTINATION,
                    payload = FingerprintCaptureContract.getArgs(flowType, fingersToCollect),
                )
            }

            Modality.FACE -> {
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

    private fun buildModalityMatcherSteps(
        projectConfiguration: ProjectConfiguration,
        flowType: FlowType,
        subjectQuery: SubjectQuery,
        biometricDataSource: BiometricDataSource,
    ) = projectConfiguration.general.modalities.map {
        Step(
            id = when (it) {
                Modality.FINGERPRINT -> StepId.FINGERPRINT_MATCHER
                Modality.FACE -> StepId.FACE_MATCHER
            },
            navigationActionId = R.id.action_orchestratorFragment_to_matcher,
            destinationId = MatchContract.DESTINATION,
            payload = MatchStepStubPayload.asBundle(flowType, subjectQuery, biometricDataSource),
        )
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
}
