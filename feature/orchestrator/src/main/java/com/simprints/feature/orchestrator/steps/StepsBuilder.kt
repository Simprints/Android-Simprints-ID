package com.simprints.feature.orchestrator.steps

import androidx.core.os.bundleOf
import com.simprints.feature.consent.ConsentContract
import com.simprints.feature.consent.ConsentType
import com.simprints.feature.enrollast.EnrolLastBiometricContract
import com.simprints.feature.fetchsubject.FetchSubjectContract
import com.simprints.feature.orchestrator.R
import com.simprints.feature.selectsubject.SelectSubjectContract
import com.simprints.feature.setup.SetupContract
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.orchestration.data.ActionRequest
import javax.inject.Inject

internal class StepsBuilder @Inject constructor() {

    fun build(action: ActionRequest, projectConfiguration: ProjectConfiguration): List<Step> =
        when (action) {
            is ActionRequest.EnrolActionRequest -> listOf(
                buildSetupStep(),
                // TODO configure modalities
                buildConsentStep(ConsentType.ENROL),
                // TODO modality steps
            )

            is ActionRequest.IdentifyActionRequest -> listOf(
                buildSetupStep(),
                // TODO configure modalities
                buildConsentStep(ConsentType.IDENTIFY),
                // TODO modality steps
            )

            is ActionRequest.VerifyActionRequest -> listOf(
                buildSetupStep(),
                // TODO configure modalities
                buildFetchGuidStep(action.projectId, action.verifyGuid),
                buildConsentStep(ConsentType.VERIFY),
                // TODO modality steps
            )

            is ActionRequest.EnrolLastBiometricActionRequest -> listOf(
                buildLastBiometricStep(action),
            )

            is ActionRequest.ConfirmActionRequest -> listOf(
                buildConfirmIdentity(action),
            )
        }

    private fun buildSetupStep() = Step(
        navigationActionId = R.id.action_orchestratorFragment_to_setup,
        destinationId = SetupContract.DESTINATION_ID,
        resultType = SetupContract.RESULT_CLASS,
        payload = bundleOf(),
    )

    private fun buildFetchGuidStep(projectId: String, subjectId: String) = Step(
        navigationActionId = R.id.action_orchestratorFragment_to_fetchSubject,
        destinationId = FetchSubjectContract.DESTINATION_ID,
        resultType = FetchSubjectContract.RESULT_CLASS,
        payload = FetchSubjectContract.getArgs(projectId, subjectId),
    )

    private fun buildConsentStep(consentType: ConsentType) = Step(
        navigationActionId = R.id.action_orchestratorFragment_to_consent,
        destinationId = ConsentContract.DESTINATION_ID,
        resultType = ConsentContract.RESULT_CLASS,
        payload = ConsentContract.getArgs(consentType),
    )

    private fun buildConfirmIdentity(action: ActionRequest.ConfirmActionRequest) = Step(
        navigationActionId = R.id.action_orchestratorFragment_to_selectSubject,
        destinationId = SelectSubjectContract.DESTINATION_ID,
        resultType = SelectSubjectContract.RESULT_CLASS,
        payload = SelectSubjectContract.getArgs(
            projectId = action.projectId,
            subjectId = action.selectedGuid,
        ),
    )

    private fun buildLastBiometricStep(action: ActionRequest.EnrolLastBiometricActionRequest) = Step(
        navigationActionId = R.id.action_orchestratorFragment_to_enrolLast,
        destinationId = EnrolLastBiometricContract.DESTINATION_ID,
        resultType = EnrolLastBiometricContract.RESULT_CLASS,
        payload = EnrolLastBiometricContract.getArgs(
            projectId = action.projectId,
            userId = action.userId,
            moduleId = action.moduleId,
            steps = emptyList(), // TODO add previous step results
        ),
    )
}
