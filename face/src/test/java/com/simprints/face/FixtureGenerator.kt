package com.simprints.face

import android.graphics.Rect
import com.simprints.eventsystem.event.domain.models.face.FaceTemplateFormat
import com.simprints.face.controllers.core.events.model.FaceCaptureBiometricsEvent
import com.simprints.face.controllers.core.events.model.FaceCaptureEvent
import com.simprints.face.data.db.person.FaceIdentity
import com.simprints.face.data.db.person.FaceSample
import com.simprints.face.data.moduleapi.face.responses.entities.FaceMatchResult
import com.simprints.face.detection.Face
import com.simprints.face.models.FaceDetection
import com.simprints.id.tools.utils.generateSequenceN
import java.util.*
import kotlin.random.Random

object FixtureGenerator {
    fun getFaceIdentity(numFaces: Int): FaceIdentity =
        FaceIdentity(
            UUID.randomUUID().toString(),
            generateSequenceN(numFaces) { getFaceSample() }.toList()
        )

    fun getFaceSample(): FaceSample =
        FaceSample(UUID.randomUUID().toString(), Random.nextBytes(20))

    fun generateFaceMatchResults(n: Int): List<FaceMatchResult> =
        generateSequenceN(n) { getFaceMatchResult() }.toList()

    fun getFaceMatchResult(): FaceMatchResult =
        FaceMatchResult(UUID.randomUUID().toString(), Random.nextFloat() * 100)

    fun getFace(rect: Rect = Rect(0, 0, 60, 60), quality: Float = 1f): Face =
        Face(
            100,
            100,
            rect,
            0f,
            0f,
            quality,
            Random.nextBytes(20),
            FaceDetection.TemplateFormat.MOCK
        )

    val faceCaptureEvent1 = FaceCaptureEvent(
        startTime = 2,
        endTime = 3,
        attemptNb = 0,
        qualityThreshold = -1.0f,
        result = FaceCaptureEvent.Result.VALID,
        isFallback = false,
        eventFace = FaceCaptureEvent.EventFace(
            yaw = 0.0f,
            roll = 0.0f,
            quality = 1.0f,
            format = FaceTemplateFormat.MOCK
        ),
        payloadId = "someId"
    )
    val faceCaptureBiometricsEvent1 = FaceCaptureBiometricsEvent(
        startTime = 2,
        endTime = 0,
        result = FaceCaptureBiometricsEvent.Result.VALID,
        eventFace = FaceCaptureBiometricsEvent.EventFace(
            template = "rR/uPLRKPI0yWzd9eQLM1/ST6DQ=",
            format = FaceTemplateFormat.MOCK
        ),
        payloadId = "someId"
    )

    val faceCaptureEvent2 = FaceCaptureEvent(
        startTime = 4,
        endTime = 5,
        attemptNb = 0,
        qualityThreshold = -1.0f,
        result = FaceCaptureEvent.Result.VALID,
        isFallback = false,
        eventFace = FaceCaptureEvent.EventFace(
            yaw = 0.0f,
            roll = 0.0f,
            quality = 1.0f,
            format = FaceTemplateFormat.MOCK
        ),
        payloadId = "someId"
    )
    val faceCaptureBiometricsEvent2 = FaceCaptureBiometricsEvent(
        startTime = 4,
        endTime = 0,
        result = FaceCaptureBiometricsEvent.Result.VALID,
        eventFace = FaceCaptureBiometricsEvent.EventFace(
            template = "rR/uPLRKPI0yWzd9eQLM1/ST6DQ=",
            format = FaceTemplateFormat.MOCK
        ),
        payloadId = "someId"
    )

    val faceCaptureEvent3 = FaceCaptureEvent(
        startTime = 0,
        endTime = 1,
        attemptNb = 0,
        qualityThreshold = -1.0f,
        result = FaceCaptureEvent.Result.VALID,
        isFallback = true,
        eventFace = FaceCaptureEvent.EventFace(
            yaw = 0.0f,
            roll = 0.0f,
            quality = 1.0f,
            format = FaceTemplateFormat.MOCK
        ),
        payloadId = "someId"
    )
    val faceCaptureBiometricsEvent3 = FaceCaptureBiometricsEvent(
        startTime = 0,
        endTime = 0,
        result = FaceCaptureBiometricsEvent.Result.VALID,
        eventFace = FaceCaptureBiometricsEvent.EventFace(
            template = "rR/uPLRKPI0yWzd9eQLM1/ST6DQ=",
            format = FaceTemplateFormat.MOCK
        ),
        payloadId = "someId"
    )
}
