package com.simprints.face.capture.usecases

import android.graphics.Rect
import javax.inject.Inject
import kotlin.math.hypot
import kotlin.math.pow

/**
 * Decides which of the detected faces is the subject being captured.
 *
 * The operator aims the camera at the person they are enrolling, so the subject is the face that
 * is both large and near the middle of the frame - which is what the aim guide drawn on the
 * preview asks them to do. Faces are therefore scored on the area they occupy, weighted by how
 * central they are, and the highest score wins.
 */
internal class SelectDominantFaceUseCase @Inject constructor() {
    /**
     * @param faces detected bounding boxes in frame pixels
     * @return index of the subject within [faces], or null if there was nothing to choose from
     */
    operator fun invoke(
        faces: List<Rect>,
        frameWidth: Int,
        frameHeight: Int,
    ): Int? {
        if (faces.isEmpty() || frameWidth <= 0 || frameHeight <= 0) return null
        return faces.indices.maxByOrNull { score(faces[it], frameWidth, frameHeight) }
    }

    private fun score(
        face: Rect,
        frameWidth: Int,
        frameHeight: Int,
    ): Float {
        val area = face.width().toFloat() * face.height() / (frameWidth.toFloat() * frameHeight)
        val centreX = frameWidth / 2f
        val centreY = frameHeight / 2f
        val offset = hypot(face.exactCenterX() - centreX, face.exactCenterY() - centreY)
        val centrality = (1f - offset / hypot(centreX, centreY)).coerceIn(0f, 1f)
        return area * centrality.pow(CENTRALITY_WEIGHT)
    }

    companion object {
        /**
         * Above 1 this pulls the choice toward the middle of the frame rather than raw size, so a
         * bystander who happens to be closer to the lens does not outrank the person being aimed at.
         */
        private const val CENTRALITY_WEIGHT = 2f
    }
}
