package com.simprints.fingerprint.integration

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.simprints.moduleapi.common.ISecuredImageRef
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.IFingerprintSample
import com.simprints.moduleapi.fingerprint.IFingerprintTemplateFormat
import com.simprints.moduleapi.fingerprint.requests.IFingerprintCaptureRequest
import com.simprints.moduleapi.fingerprint.requests.IFingerprintMatchRequest
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest
import kotlinx.parcelize.Parcelize
import java.io.Serializable

fun createFingerprintCaptureRequestIntent(
    fingerprintsToCapture: List<IFingerIdentifier> =
        DEFAULT_FINGERS_TO_CAPTURE
): Intent = Intent()
    .setClassName(
        InstrumentationRegistry.getInstrumentation().targetContext.applicationContext,
        "com.simprints.fingerprint.activities.orchestrator.OrchestratorActivity"
    )
    .putExtra(
        IFingerprintRequest.BUNDLE_KEY,
        TestFingerprintCaptureRequest(fingerprintsToCapture)
    )


val DEFAULT_FINGERS_TO_CAPTURE = listOf(
    IFingerIdentifier.LEFT_THUMB,
    IFingerIdentifier.LEFT_INDEX_FINGER
)

@Parcelize
private class IFingerprintSampleImpl(
    override val fingerIdentifier: IFingerIdentifier,
    override val template: ByteArray,
    override val templateQualityScore: Int,
    override val format: IFingerprintTemplateFormat,
    override val imageRef: ISecuredImageRef?
) : IFingerprintSample

@Parcelize
data class TestFingerprintCaptureRequest(override val fingerprintsToCapture: List<IFingerIdentifier>) :
    IFingerprintCaptureRequest

@Parcelize
data class TestFingerprintMatchRequest(
    override val probeFingerprintSamples: List<IFingerprintSample>,
    override val queryForCandidates: Serializable
) : IFingerprintMatchRequest
