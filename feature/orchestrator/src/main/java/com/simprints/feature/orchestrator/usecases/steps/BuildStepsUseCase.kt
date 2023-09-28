package com.simprints.feature.orchestrator.usecases.steps

import androidx.core.os.bundleOf
import com.simprints.core.DeviceID
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.common.FlowProvider
import com.simprints.face.capture.FaceCaptureContract
import com.simprints.face.configuration.FaceConfigurationContract
import com.simprints.face.matcher.FaceMatchContract
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
import com.simprints.feature.selectsubject.SelectSubjectContract
import com.simprints.feature.setup.SetupContract
import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import com.simprints.infra.orchestration.data.ActionRequest
import javax.inject.Inject

@ExcludedFromGeneratedTestCoverageReports("Mapping code for steps")
internal class BuildStepsUseCase @Inject constructor(
    @DeviceID private val deviceId: String,
    private val buildMatcherSubjectQuery: BuildMatcherSubjectQueryUseCase,
    private val cache: OrchestratorCache,
    private val mapStepsForLastBiometrics: MapStepsForLastBiometricEnrolUseCase,
) {

    fun build(action: ActionRequest, projectConfiguration: ProjectConfiguration) = when (action) {
        is ActionRequest.EnrolActionRequest -> listOf(
            buildSetupStep(),
            buildModalityConfigurationSteps(projectConfiguration, action.projectId, deviceId),
            buildConsentStep(ConsentType.ENROL),
            buildModalityCaptureSteps(projectConfiguration),
            if (projectConfiguration.general.duplicateBiometricEnrolmentCheck) {
                buildModalityMatcherSteps(
                    projectConfiguration,
                    FlowProvider.FlowType.ENROL,
                    buildMatcherSubjectQuery(projectConfiguration, action),
                )
            } else emptyList(),
        )

        is ActionRequest.IdentifyActionRequest -> listOf(
            buildSetupStep(),
            buildModalityConfigurationSteps(projectConfiguration, action.projectId, deviceId),
            buildConsentStep(ConsentType.IDENTIFY),
            buildModalityCaptureSteps(projectConfiguration),
            buildModalityMatcherSteps(
                projectConfiguration,
                FlowProvider.FlowType.IDENTIFY,
                buildMatcherSubjectQuery(projectConfiguration, action),
            )
        )

        is ActionRequest.VerifyActionRequest -> listOf(
            buildSetupStep(),
            buildModalityConfigurationSteps(projectConfiguration, action.projectId, deviceId),
            buildFetchGuidStep(action.projectId, action.verifyGuid),
            buildConsentStep(ConsentType.VERIFY),
            buildModalityCaptureSteps(projectConfiguration),
            buildModalityMatcherSteps(
                projectConfiguration,
                FlowProvider.FlowType.VERIFY,
                SubjectQuery(subjectId = action.verifyGuid),
            )
        )

        is ActionRequest.EnrolLastBiometricActionRequest -> listOf(
            buildLastBiometricStep(action),
        )

        is ActionRequest.ConfirmActionRequest -> listOf(
            buildConfirmIdentityStep(action),
        )
    }.flatten()

    private fun buildSetupStep() = listOf(Step(
        id = StepId.SETUP,
        navigationActionId = R.id.action_orchestratorFragment_to_setup,
        destinationId = SetupContract.DESTINATION_ID,
        payload = bundleOf(),
    ))

    private fun buildModalityConfigurationSteps(
        projectConfiguration: ProjectConfiguration,
        projectId: String,
        deviceId: String,
    ): List<Step> = projectConfiguration.general.modalities.map {
        when (it) {
            Modality.FINGERPRINT -> TODO("Fingerprint modality is not supported yet")

            Modality.FACE -> Step(
                id = StepId.FACE_CONFIGURATION,
                navigationActionId = R.id.action_orchestratorFragment_to_faceConfiguration,
                destinationId = FaceConfigurationContract.DESTINATION_ID,
                payload = FaceConfigurationContract.getArgs(projectId, deviceId),
            )
        }
    }

    private fun buildFetchGuidStep(projectId: String, subjectId: String) = listOf(Step(
        id = StepId.FETCH_GUID,
        navigationActionId = R.id.action_orchestratorFragment_to_fetchSubject,
        destinationId = FetchSubjectContract.DESTINATION_ID,
        payload = FetchSubjectContract.getArgs(projectId, subjectId),
    ))

    private fun buildConsentStep(consentType: ConsentType) = listOf(Step(
        id = StepId.CONSENT,
        navigationActionId = R.id.action_orchestratorFragment_to_consent,
        destinationId = ConsentContract.DESTINATION_ID,
        payload = ConsentContract.getArgs(consentType),
    ))


    private fun buildModalityCaptureSteps(projectConfiguration: ProjectConfiguration) = projectConfiguration.general.modalities.map {
        when (it) {
            Modality.FINGERPRINT -> TODO("Fingerprint modality is not supported yet")

            Modality.FACE -> {
                val samplesToCapture = projectConfiguration.face?.nbOfImagesToCapture ?: 0
                Step(
                    id = StepId.FACE_CAPTURE,
                    navigationActionId = R.id.action_orchestratorFragment_to_faceCapture,
                    destinationId = FaceCaptureContract.DESTINATION_ID,
                    payload = FaceCaptureContract.getArgs(samplesToCapture),
                )
            }
        }
    }

    private fun buildModalityMatcherSteps(
        projectConfiguration: ProjectConfiguration,
        flowType: FlowProvider.FlowType,
        subjectQuery: SubjectQuery,
    ) = projectConfiguration.general.modalities.map {
        when (it) {
            Modality.FINGERPRINT -> TODO("Fingerprint modality is not supported yet")

            Modality.FACE -> Step(
                id = StepId.FACE_MATCHER,
                navigationActionId = R.id.action_orchestratorFragment_to_faceMatcher,
                destinationId = FaceMatchContract.DESTINATION_ID,
                payload = bundleOf(MatchStepStubPayload.STUB_KEY to MatchStepStubPayload(flowType, subjectQuery)),
            )
        }
    }

    private fun buildLastBiometricStep(action: ActionRequest.EnrolLastBiometricActionRequest) = listOf(Step(
        id = StepId.LAST_BIOMETRIC,
        navigationActionId = R.id.action_orchestratorFragment_to_enrolLast,
        destinationId = EnrolLastBiometricContract.DESTINATION_ID,
        payload = EnrolLastBiometricContract.getArgs(
            projectId = action.projectId,
            userId = action.userId,
            moduleId = action.moduleId,
            steps = mapStepsForLastBiometrics(cache.steps.mapNotNull { it.result }),
        ),
    ))

    private fun buildConfirmIdentityStep(action: ActionRequest.ConfirmActionRequest) = listOf(Step(
        id = StepId.CONFIRM_IDENTITY,
        navigationActionId = R.id.action_orchestratorFragment_to_selectSubject,
        destinationId = SelectSubjectContract.DESTINATION_ID,
        payload = SelectSubjectContract.getArgs(
            projectId = action.projectId,
            subjectId = action.selectedGuid,
        ),
    ))
}
