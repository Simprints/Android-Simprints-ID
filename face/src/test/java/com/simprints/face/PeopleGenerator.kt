package com.simprints.face

import com.simprints.face.data.db.person.FaceIdentity
import com.simprints.face.data.db.person.FaceSample
import com.simprints.face.data.moduleapi.face.responses.entities.FaceMatchResult
import com.simprints.id.tools.utils.generateSequenceN
import java.util.*
import kotlin.random.Random

object PeopleGenerator {
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
}
