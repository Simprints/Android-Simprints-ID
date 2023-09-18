package com.simprints.face

import android.graphics.Rect
import com.simprints.infra.facebiosdk.detection.Face
import kotlin.random.Random

object FixtureGenerator {

    fun getFace(rect: Rect = Rect(0, 0, 60, 60), quality: Float = 1f): Face {
        return Face(
            100,
            100,
            rect,
            0f,
            0f,
            quality,
            Random.nextBytes(20),
            "format"
        )
    }

}
