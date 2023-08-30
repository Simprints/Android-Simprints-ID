package com.simprints.id.orchestrator.responsebuilders

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
import com.simprints.id.orchestrator.steps.core.response.ExitFormResponse
import com.simprints.infra.config.domain.models.Finger
import com.simprints.infra.events.sampledata.FACE_TEMPLATE_FORMAT
import io.mockk.mockk

fun mockFingerprintCaptureStep(): Step {
    val request = FingerprintCaptureRequest(
        fingerprintsToCapture = listOf(Finger.LEFT_THUMB, Finger.LEFT_INDEX_FINGER)
    )

    return Step(
        requestCode = 123,
        activityName = "com.simprints.id.MyFingerprintActivity",
        bundleKey = "BUNDLE_KEY",
        payloadType = Step.PayloadType.REQUEST,
        payload = request,
        result = FingerprintCaptureResponse(
            captureResult =
            listOf(
                FingerprintCaptureResult(
                    Finger.LEFT_THUMB,
                    FingerprintCaptureSample(
                        Finger.LEFT_THUMB,
                        templateQualityScore = 10,
                        template = "template".toByteArray(),
                        format = "ISO_19794_2"
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
        payloadType = Step.PayloadType.REQUEST,
        payload = request,
        result = FingerprintMatchResponse(buildMatchResults(includeHighMatch)),
        status = Step.Status.COMPLETED
    )
}

fun mockEmptyFingerprintMatchStep() = Step(
    requestCode = 234,
    activityName = "com.simprints.id.MyFingerprintActivity",
    bundleKey = "BUNDLE_KEY",
    payloadType = Step.PayloadType.REQUEST,
    payload = FingerprintMatchRequest(mockk(), mockk()),
    result = FingerprintMatchResponse(listOf()),
    status = Step.Status.COMPLETED
)

private fun buildMatchResults(includeHighMatch: Boolean) = if (includeHighMatch) {
    listOf(
        FingerprintMatchResult("person_id", 40f),
        FingerprintMatchResult("person_id2", 15f),
        FingerprintMatchResult("person_id3", 10f)
    )
} else {
    listOf(
        FingerprintMatchResult("person_id", 15f),
        FingerprintMatchResult("person_id2", 30f),
        FingerprintMatchResult("person_id3", 10f)
    )
}

fun mockFaceCaptureStep(): Step {
    val request = FaceCaptureRequest(nFaceSamplesToCapture = 2)
    val response = FaceCaptureResponse(
        listOf(
            FaceCaptureResult(
                index = 0,
                result = FaceCaptureSample(
                    "faceId",
                    "faceId".toByteArray(),
                    null,
                    FACE_TEMPLATE_FORMAT
                )
            )
        )
    )

    return Step(
        requestCode = 321,
        activityName = "com.simprints.id.MyFaceActivity",
        bundleKey = "BUNDLE_KEY",
        payloadType = Step.PayloadType.REQUEST,
        payload = request,
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
        payloadType = Step.PayloadType.REQUEST,
        payload = request,
        result = response,
        status = Step.Status.COMPLETED
    )
}

fun mockEmptyFaceMatchStep() = Step(
    requestCode = 322,
    activityName = "com.simprints.id.MyFaceActivity",
    bundleKey = "BUNDLE_KEY",
    payloadType = Step.PayloadType.REQUEST,
    payload = FaceMatchRequest(mockk(), mockk()),
    result = FaceMatchResponse(listOf()),
    status = Step.Status.COMPLETED
)

fun mockCoreExitFormResponse() = Step(
    requestCode = 0,
    activityName = "",
    bundleKey = "",
    payloadType = Step.PayloadType.REQUEST,
    payload = mockk(),
    result = ExitFormResponse(),
    status = Step.Status.COMPLETED
)

private fun buildMatchResultsForFace(includeHighMatch: Boolean) = if (includeHighMatch) {
    listOf(
        FaceMatchResult(guidFound = "guid", confidence = 40f),
        FaceMatchResult(guidFound = "guid2", confidence = 15f),
        FaceMatchResult(guidFound = "guid3", confidence = 30f),
        FaceMatchResult(guidFound = "guid4", confidence = 10f),
    )
} else {
    listOf(
        FaceMatchResult(guidFound = "guid", confidence = 35f),
        FaceMatchResult(guidFound = "guid2", confidence = 15f),
        FaceMatchResult(guidFound = "guid3", confidence = 30f),
        FaceMatchResult(guidFound = "guid4", confidence = 10f),
    )
}
