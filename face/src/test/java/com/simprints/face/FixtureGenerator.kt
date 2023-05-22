package com.simprints.face

import android.graphics.Rect
import com.simprints.face.data.moduleapi.face.responses.entities.FaceMatchResult
import com.simprints.infra.facebiosdk.detection.Face
import com.simprints.infra.facebiosdk.matching.FaceIdentity
import com.simprints.infra.facebiosdk.matching.FaceSample
import java.util.UUID
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

    const val FACE_TEMPLATE_FORMAT = "format"
    fun getFace(rect: Rect = Rect(0, 0, 60, 60), quality: Float = 1f): Face {
        return Face(
            100,
            100,
            rect,
            0f,
            0f,
            quality,
            Random.nextBytes(20),
            FACE_TEMPLATE_FORMAT
        )
    }

    fun <T : Any> generateSequenceN(n: Int, f: () -> T) = generateSequence(f).take(n)
}
