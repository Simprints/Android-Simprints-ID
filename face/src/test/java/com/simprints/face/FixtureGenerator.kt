package com.simprints.face

import android.graphics.Rect
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
        Face(100, 100, rect, 0f, 0f, quality, Random.nextBytes(20), FaceDetection.TemplateFormat.MOCK)
}
