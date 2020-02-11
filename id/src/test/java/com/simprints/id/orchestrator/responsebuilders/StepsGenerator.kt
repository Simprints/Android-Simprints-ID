package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.data.db.person.domain.FingerIdentifier
import com.simprints.id.domain.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceMatchRequest
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceMatchResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceMatchResult
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintMatchRequest
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintMatchResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureSample
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintMatchResult
import com.simprints.id.orchestrator.steps.Step
import io.mockk.mockk

fun mockFingerprintCaptureStep(): Step {
    val request = FingerprintCaptureRequest(
        fingerprintsToCapture = listOf(FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_INDEX_FINGER)
    )

    return Step(
        requestCode = 123,
        activityName = "com.simprints.id.MyFingerprintActivity",
        bundleKey = "BUNDLE_KEY",
        request = request,
        result = FingerprintCaptureResponse(captureResult =
        listOf(
            FingerprintCaptureResult(
                FingerIdentifier.LEFT_THUMB,
                FingerprintCaptureSample(
                    FingerIdentifier.LEFT_THUMB,
                    templateQualityScore = 10,
                    template = "template".toByteArray()
                )
            )
        )
        ),
        status = Step.Status.COMPLETED
    )
}

fun mockFingerprintMatchStep(): Step {
    val request = FingerprintMatchRequest(mockk(), mockk())

    return Step(
        requestCode = 234,
        activityName = "com.simprints.id.MyFingerprintActivity",
        bundleKey = "BUNDLE_KEY",
        request = request,
        result = FingerprintMatchResponse(listOf(
            FingerprintMatchResult("person_id", 75f)
        )),
        status = Step.Status.COMPLETED
    )
}

fun mockFaceCaptureStep(): Step {
    val request = FaceCaptureRequest(nFaceSamplesToCapture = 2)
    val response = FaceCaptureResponse(
        listOf(
            FaceCaptureResult(
                index = 0,
                result = FaceCaptureSample("faceId", "faceId".toByteArray(), null)
            )
        )
    )

    return Step(
        requestCode = 321,
        activityName = "com.simprints.id.MyFaceActivity",
        bundleKey = "BUNDLE_KEY",
        request = request,
        result = response,
        status = Step.Status.COMPLETED
    )
}

fun mockFaceMatchStep(): Step {
    val request = FaceMatchRequest(mockk(), mockk())

    val response =
        FaceMatchResponse(listOf(FaceMatchResult(guidFound = "guid", confidence = 75f)))

    return Step(
        requestCode = 322,
        activityName = "com.simprints.id.MyFaceActivity",
        bundleKey = "BUNDLE_KEY",
        request = request,
        result = response,
        status = Step.Status.COMPLETED
    )
}
