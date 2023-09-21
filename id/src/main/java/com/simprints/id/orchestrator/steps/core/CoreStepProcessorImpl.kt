package com.simprints.id.orchestrator.steps.core

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.bundleOf
import com.simprints.feature.consent.ConsentContract
import com.simprints.feature.consent.ConsentResult
import com.simprints.feature.consent.ConsentType
import com.simprints.feature.consent.screens.ConsentWrapperActivity
import com.simprints.feature.enrollast.EnrolLastBiometricContract
import com.simprints.feature.enrollast.EnrolLastBiometricResult
import com.simprints.feature.enrollast.EnrolLastBiometricWrapperActivity
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.fetchsubject.FetchSubjectContract
import com.simprints.feature.fetchsubject.FetchSubjectResult
import com.simprints.feature.fetchsubject.FetchSubjectWrapperActivity
import com.simprints.feature.selectsubject.SelectSubjectContract
import com.simprints.feature.selectsubject.SelectSubjectWrapperActivity
import com.simprints.feature.setup.SetupContract
import com.simprints.feature.setup.SetupWrapperActivity
import com.simprints.id.exitformhandler.ExitFormReason.Companion.fromExitFormOption
import com.simprints.id.orchestrator.steps.MapStepsForLastBiometricEnrolUseCase
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreRequestCode.CONSENT
import com.simprints.id.orchestrator.steps.core.CoreRequestCode.FETCH_GUID_CHECK
import com.simprints.id.orchestrator.steps.core.CoreRequestCode.GUID_SELECTION_CODE
import com.simprints.id.orchestrator.steps.core.CoreRequestCode.LAST_BIOMETRICS_CORE
import com.simprints.id.orchestrator.steps.core.response.AskConsentResponse
import com.simprints.id.orchestrator.steps.core.response.ConsentResponse
import com.simprints.id.orchestrator.steps.core.response.CoreResponse
import com.simprints.id.orchestrator.steps.core.response.CoreResponse.Companion.CORE_STEP_BUNDLE
import com.simprints.id.orchestrator.steps.core.response.CoreResponseType
import com.simprints.id.orchestrator.steps.core.response.EnrolLastBiometricsResponse
import com.simprints.id.orchestrator.steps.core.response.ExitFormResponse
import com.simprints.id.orchestrator.steps.core.response.FetchGUIDResponse
import com.simprints.id.orchestrator.steps.core.response.GuidSelectionResponse
import com.simprints.id.orchestrator.steps.core.response.SetupResponse
import javax.inject.Inject

class CoreStepProcessorImpl @Inject constructor(
    private val mapStepsForLastBiometricEnrol: MapStepsForLastBiometricEnrolUseCase
) : CoreStepProcessor {

    companion object {
        const val SETUP_ACTIVITY_NAME = "com.simprints.feature.setup.SetupWrapperActivity"
        const val CONSENT_ACTIVITY_NAME = "com.simprints.feature.consent.screens.ConsentWrapperActivity"
        const val FETCH_GUID_ACTIVITY_NAME =
            "com.simprints.feature.fetchsubject.FetchSubjectWrapperActivity"
        const val GUID_SELECTION_ACTIVITY_NAME =
            "com.simprints.feature.selectsubject.SelectSubjectWrapperActivity"
        const val LAST_BIOMETRICS_CORE_ACTIVITY_NAME =
            "com.simprints.feature.enrollast.EnrolLastBiometricWrapperActivity"
    }

    override fun buildStepSetup(): Step = Step(
        requestCode = CoreRequestCode.SETUP.value,
        activityName = SETUP_ACTIVITY_NAME,
        bundleKey = SetupWrapperActivity.SETUP_ARGS_EXTRA,
        payloadType = Step.PayloadType.BUNDLE,
        payload = bundleOf(),
        status = Step.Status.NOT_STARTED,
    )

    override fun buildStepConsent(consentType: ConsentType) = Step(
        requestCode = CONSENT.value,
        activityName = CONSENT_ACTIVITY_NAME,
        bundleKey = ConsentWrapperActivity.CONSENT_ARGS_EXTRA,
        payloadType = Step.PayloadType.BUNDLE,
        payload = ConsentContract.getArgs(consentType),
        status = Step.Status.NOT_STARTED
    )

    override fun buildFetchGuidStep(projectId: String, verifyGuid: String) = Step(
        requestCode = FETCH_GUID_CHECK.value,
        activityName = FETCH_GUID_ACTIVITY_NAME,
        bundleKey = FetchSubjectWrapperActivity.FETCH_SUBJECT_ARGS_EXTRA,
        payloadType = Step.PayloadType.BUNDLE,
        payload = FetchSubjectContract.getArgs(projectId, verifyGuid),
        status = Step.Status.NOT_STARTED
    )

    override fun buildConfirmIdentityStep(
        projectId: String,
        selectedGuid: String
    ) = Step(
        requestCode = GUID_SELECTION_CODE.value,
        activityName = GUID_SELECTION_ACTIVITY_NAME,
        bundleKey = SelectSubjectWrapperActivity.SELECT_SUBJECT_ARGS_EXTRA,
        payloadType = Step.PayloadType.BUNDLE,
        payload = SelectSubjectContract.getArgs(projectId, selectedGuid),
        status = Step.Status.NOT_STARTED
    )

    override fun buildAppEnrolLastBiometricsStep(
        projectId: String,
        userId: String,
        moduleId: String,
        previousSteps: List<Step>,
        sessionId: String?
    ) = Step(
        requestCode = LAST_BIOMETRICS_CORE.value,
        activityName = LAST_BIOMETRICS_CORE_ACTIVITY_NAME,
        bundleKey = EnrolLastBiometricWrapperActivity.ENROL_LAST_ARGS_EXTRA,
        payloadType = Step.PayloadType.BUNDLE,
        payload = EnrolLastBiometricContract.getArgs(projectId, userId, moduleId, mapStepsForLastBiometricEnrol(previousSteps)),
        status = Step.Status.NOT_STARTED
    )

    override fun processResult(data: Intent?): Step.Result? {
        val legacyCoreResponse = data?.getParcelableExtra<CoreResponse>(CORE_STEP_BUNDLE)

        return if (legacyCoreResponse != null) {
            when (legacyCoreResponse.type) {
                CoreResponseType.EXIT_FORM -> data.getParcelableExtra<ExitFormResponse>(CORE_STEP_BUNDLE)
                else -> null // No-op, data will not have response with key CORE_STEP_BUNDLE
            }
        } else {
            data?.extras?.let { handleFeatureModuleResponses(it) }
        }
    }

    private fun handleFeatureModuleResponses(data: Bundle): Step.Result? = when {
        data.containsKey(ConsentContract.CONSENT_RESULT) -> {
            when (val result = data.getParcelable<Parcelable>(ConsentContract.CONSENT_RESULT)) {
                is ConsentResult -> AskConsentResponse(if (result.accepted) ConsentResponse.ACCEPTED else ConsentResponse.DECLINED)
                is ExitFormResult -> mapExitFormResponse(result)
                else -> null
            }
        }

        data.containsKey(FetchSubjectContract.FETCH_SUBJECT_RESULT) -> {
            when (val result = data.getParcelable<Parcelable>(FetchSubjectContract.FETCH_SUBJECT_RESULT)) {
                is ExitFormResult -> mapExitFormResponse(result)
                is FetchSubjectResult -> FetchGUIDResponse(result.found, result.wasOnline)
                else -> null
            }
        }

        data.containsKey(EnrolLastBiometricContract.ENROL_LAST_RESULT) -> {
            when (val result = data.getParcelable<Parcelable>(EnrolLastBiometricContract.ENROL_LAST_RESULT)) {
                is EnrolLastBiometricResult -> EnrolLastBiometricsResponse(result.newSubjectId)
                else -> null
            }
        }

        // We always return identification successful
        data.containsKey(SelectSubjectContract.SELECT_SUBJECT_RESULT) -> GuidSelectionResponse(identificationOutcome = true)

        // We always return setup successful
        data.containsKey(SetupContract.SETUP_RESULT) -> SetupResponse(isSetupComplete = true)

        else -> null
    }

    private fun mapExitFormResponse(result: ExitFormResult) = result.submittedOption()
        ?.let { ExitFormResponse(fromExitFormOption(it), result.reason.orEmpty()) }

}
