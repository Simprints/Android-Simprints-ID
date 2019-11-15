package com.simprints.fingerprint.integration

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.simprints.fingerprint.controllers.core.eventData.model.fromDomainToCore
import com.simprints.fingerprint.data.domain.fingerprint.Fingerprint
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

fun createFingerprintCaptureRequestIntent(fingerprintsToCapture: List<IFingerIdentifier> =
                                              DEFAULT_FINGERS_TO_CAPTURE): Intent = Intent()
    .setClassName(ApplicationProvider.getApplicationContext<Application>().packageName,
        FingerprintStepProcessorImpl.ACTIVITY_CLASS_NAME)
    .putExtra(IFingerprintRequest.BUNDLE_KEY,
        TestFingerprintCaptureRequest(fingerprintsToCapture))

fun createFingerprintMatchRequestIntent(probeFingerprints: List<Fingerprint>,
                                        queryForCandidates: Serializable): Intent = Intent()
    .setClassName(ApplicationProvider.getApplicationContext<Application>().packageName,
        FingerprintStepProcessorImpl.ACTIVITY_CLASS_NAME)
    .putExtra(IFingerprintRequest.BUNDLE_KEY,
        TestFingerprintMatchRequest(
            probeFingerprints.map { it.fromDomainToCore().fromDomainToModuleApi() },
            queryForCandidates))

val DEFAULT_FINGERS_TO_CAPTURE = listOf(
    IFingerIdentifier.LEFT_THUMB,
    IFingerIdentifier.LEFT_INDEX_FINGER
)

@Parcelize
data class TestFingerprintCaptureRequest(override val fingerprintsToCapture: List<IFingerIdentifier>) : IFingerprintCaptureRequest

@Parcelize
data class TestFingerprintMatchRequest(override val probeFingerprintSamples: List<IFingerprintSample>,
                                       override val queryForCandidates: Serializable) : IFingerprintMatchRequest
