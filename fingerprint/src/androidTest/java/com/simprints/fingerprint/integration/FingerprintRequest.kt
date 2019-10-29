package com.simprints.fingerprint.integration

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.simprints.fingerprint.commontesttools.generators.FingerprintGeneratorUtils
import com.simprints.fingerprint.data.domain.fingerprint.fromDomainToCore
import com.simprints.id.Application
import com.simprints.id.data.db.person.domain.fromDomainToModuleApi
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessorImpl
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.IFingerprintSample
import com.simprints.moduleapi.fingerprint.requests.IFingerprintCaptureRequest
import com.simprints.moduleapi.fingerprint.requests.IFingerprintMatchRequest
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

fun createFingerprintCaptureRequestIntent(): Intent = Intent()
    .setClassName(ApplicationProvider.getApplicationContext<Application>().packageName,
        FingerprintStepProcessorImpl.ACTIVITY_CLASS_NAME)
    .putExtra(IFingerprintRequest.BUNDLE_KEY,
        TestFingerprintCaptureRequest(DEFAULT_FINGERS_TO_CAPTURE))

fun createFingerprintRequestIntent(): Intent = Intent()
    .setClassName(ApplicationProvider.getApplicationContext<Application>().packageName,
        FingerprintStepProcessorImpl.ACTIVITY_CLASS_NAME)
    .putExtra(IFingerprintRequest.BUNDLE_KEY,
        TestFingerprintMatchRequest(
            List(2) { FingerprintGeneratorUtils.generateRandomFingerprint().fromDomainToCore().fromDomainToModuleApi() },
            TODO("Create Serializable query")))

val DEFAULT_FINGERS_TO_CAPTURE = listOf(
    IFingerIdentifier.LEFT_THUMB,
    IFingerIdentifier.LEFT_INDEX_FINGER
)

@Parcelize
data class TestFingerprintCaptureRequest(override val fingerprintsToCapture: List<IFingerIdentifier>) : IFingerprintCaptureRequest

@Parcelize
data class TestFingerprintMatchRequest(override val probeFingerprintSamples: List<IFingerprintSample>,
                                       override val queryForCandidates: Serializable) : IFingerprintMatchRequest
