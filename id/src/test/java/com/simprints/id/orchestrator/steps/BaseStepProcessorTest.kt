package com.simprints.id.orchestrator.steps

import android.os.Parcelable
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.consent.screens.ConsentWrapperActivity
import com.simprints.feature.fetchsubject.FetchSubjectWrapperActivity
import com.simprints.feature.selectsubject.SelectSubjectWrapperActivity
import com.simprints.feature.setup.SetupWrapperActivity
import com.simprints.id.orchestrator.steps.core.CoreRequestCode

open class BaseStepProcessorTest {

    protected inline fun <reified T : Parcelable> verifySetupIntent(step: Step) = verifyStep<T>(
        step,
        CoreRequestCode.SETUP.value,
        "com.simprints.feature.setup.SetupWrapperActivity",
        SetupWrapperActivity.SETUP_ARGS_EXTRA,
    )

    protected inline fun <reified T : Parcelable> verifyFingerprintIntent(step: Step, expectedRequestCode: Int) = verifyStep<T>(
        step,
        expectedRequestCode,
        "com.simprints.fingerprint.activities.orchestrator.OrchestratorActivity",
        "FingerprintRequestBundleKey",
    )

    protected inline fun <reified T : Parcelable> verifyFaceIntent(step: Step, expectedRequestCode: Int) = verifyStep<T>(
        step,
        expectedRequestCode,
        "com.simprints.face.orchestrator.FaceOrchestratorActivity",
        "FaceRequestBundleKey",
    )

    protected inline fun <reified T : Parcelable> verifyConsentIntent(step: Step) = verifyStep<T>(
        step,
        CoreRequestCode.CONSENT.value,
        "com.simprints.feature.consent.screens.ConsentWrapperActivity",
        ConsentWrapperActivity.CONSENT_ARGS_EXTRA,
    )

    protected inline fun <reified T : Parcelable> verifyFetchGuidIntent(step: Step) = verifyStep<T>(
        step,
        CoreRequestCode.FETCH_GUID_CHECK.value,
        "com.simprints.feature.fetchsubject.FetchSubjectWrapperActivity",
        FetchSubjectWrapperActivity.FETCH_SUBJECT_ARGS_EXTRA
    )

    protected inline fun <reified T : Parcelable> verifyGuidSelectedIntent(step: Step) = verifyStep<T>(
        step,
        CoreRequestCode.GUID_SELECTION_CODE.value,
        "com.simprints.feature.selectsubject.SelectSubjectWrapperActivity",
        SelectSubjectWrapperActivity.SELECT_SUBJECT_ARGS_EXTRA
    )

    protected inline fun <reified T : Parcelable> verifyStep(step: Step, expectedRequestCode: Int, activityName: String, bundleKey: String) {
        assertThat(step.activityName).isEqualTo(activityName)
        assertThat(step.requestCode).isEqualTo(expectedRequestCode)
        assertThat(step.bundleKey).isEqualTo(bundleKey)
        assertThat(step.request).isInstanceOf(T::class.java)
        assertThat(step.getResult()).isNull()
        assertThat(step.getStatus()).isEqualTo(Step.Status.NOT_STARTED)
    }
}
