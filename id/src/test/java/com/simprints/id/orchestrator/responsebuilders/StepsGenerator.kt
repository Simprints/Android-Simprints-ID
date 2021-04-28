package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.data.db.event.domain.models.face.FaceTemplateFormat
import com.simprints.id.data.db.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.id.data.db.subject.domain.FingerIdentifier
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
        result = FingerprintCaptureResponse(
            captureResult =
            listOf(
                FingerprintCaptureResult(
                    FingerIdentifier.LEFT_THUMB,
                    FingerprintCaptureSample(
                        FingerIdentifier.LEFT_THUMB,
                        templateQualityScore = 10,
                        template = "template".toByteArray(),
                        format = FingerprintTemplateFormat.ISO_19794_2
                    )
                )
            )
        ),
        status = Step.Status.COMPLETED
    )
}

fun mockFingerprintMatchStep(includeHighMatch: Boolean = true): Step {
    val request = FingerprintMatchRequest(mockk(), mockk())

    return Step(
        requestCode = 234,
        activityName = "com.simprints.id.MyFingerprintActivity",
        bundleKey = "BUNDLE_KEY",
        request = request,
        result = FingerprintMatchResponse(buildMatchResults(includeHighMatch)),
        status = Step.Status.COMPLETED
    )
}

private fun buildMatchResults(includeHighMatch: Boolean) = if (includeHighMatch) {
    listOf(
        FingerprintMatchResult("person_id", 40f),
        FingerprintMatchResult("person_id2", 15f)
    )
} else {
    listOf(
        FingerprintMatchResult("person_id2", 15f),
        FingerprintMatchResult("person_id2", 30f)
    )
}

fun mockFaceCaptureStep(): Step {
    val request = FaceCaptureRequest(nFaceSamplesToCapture = 2)
    val response = FaceCaptureResponse(
        listOf(
            FaceCaptureResult(
                index = 0,
                result = FaceCaptureSample("faceId", "faceId".toByteArray(), null, FaceTemplateFormat.RANK_ONE_1_23)
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

fun mockFaceMatchStep(includeHighMatch: Boolean = true): Step {
    val request = FaceMatchRequest(mockk(), mockk())

    val response =
        FaceMatchResponse(buildMatchResultsForFace(includeHighMatch))

    return Step(
        requestCode = 322,
        activityName = "com.simprints.id.MyFaceActivity",
        bundleKey = "BUNDLE_KEY",
        request = request,
        result = response,
        status = Step.Status.COMPLETED
    )
}

private fun buildMatchResultsForFace(includeHighMatch: Boolean) = if (includeHighMatch) {
    listOf(
        FaceMatchResult(guidFound = "guid", confidence = 40f),
        FaceMatchResult(guidFound = "guid2", confidence = 15f),
        FaceMatchResult(guidFound = "guid3", confidence = 30f)
    )
} else {
    listOf(
        FaceMatchResult(guidFound = "guid", confidence = 35f),
        FaceMatchResult(guidFound = "guid2", confidence = 15f),
        FaceMatchResult(guidFound = "guid3", confidence = 30f)
    )
}
