package com.simprints.id.orchestrator.steps

import android.os.Parcelable
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.consent.screens.ConsentWrapperActivity

open class BaseStepProcessorTest {

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

    protected inline fun <reified T : Parcelable> verifyConsentIntent(step: Step, expectedRequestCode: Int) = verifyStep<T>(
        step,
        expectedRequestCode,
        "com.simprints.feature.consent.screens.ConsentWrapperActivity",
        ConsentWrapperActivity.CONSENT_ARGS_EXTRA,
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
