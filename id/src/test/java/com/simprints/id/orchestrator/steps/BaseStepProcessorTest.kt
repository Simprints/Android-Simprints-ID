package com.simprints.id.orchestrator.steps

import android.os.Parcelable
import com.google.common.truth.Truth.assertThat

open class BaseStepProcessorTest {

    protected inline fun <reified T : Parcelable> verifyFingerprintIntent(step: Step, expectedRequestCode: Int) =
        verifyStep<T>(
            step,
            expectedRequestCode,
            "com.simprints.fingerprint.activities.launch.LaunchActivity",
            "FingerprintRequestBundleKey")

    protected inline fun <reified T : Parcelable> verifyFaceIntent(step: Step, expectedRequestCode: Int) =
        verifyStep<T>(
            step,
            expectedRequestCode,
            "com.simprints.face.activities.FaceCaptureActivity",
            "FaceRequestBundleKey")

    protected inline fun <reified T : Parcelable> verifyStep(step: Step, expectedRequestCode: Int, activityName: String, bundleKey: String) {
        assertThat(step.activityName).isEqualTo(activityName)
        assertThat(step.requestCode).isEqualTo(expectedRequestCode)
        assertThat(step.bundleKey).isEqualTo(bundleKey)
        assertThat(step.request).isInstanceOf(T::class.java)
        assertThat(step.result).isNull()
        assertThat(step.status).isEqualTo(Step.Status.NOT_STARTED)
    }
}
