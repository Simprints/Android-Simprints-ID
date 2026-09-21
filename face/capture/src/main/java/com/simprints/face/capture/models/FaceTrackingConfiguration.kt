package com.simprints.face.capture.models

import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.FACE_TRACKING_MAX_IMAGE_SIZE_PX_DEFAULT
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.FACE_TRACKING_MIN_FACE_SIZE_PX_DEFAULT

/**
 * How face tracking behaves for this project.
 *
 * The two sizes bracket the crop kept around the subject: anything smaller than [minFaceSizePx] is
 * rejected as too far to yield a template, and anything larger than [maxImageSizePx] is scaled down
 * before it is stored and uploaded.
 */
internal data class FaceTrackingConfiguration(
    val enabled: Boolean,
    val minFaceSizePx: Int,
    val maxImageSizePx: Int,
) {
    companion object {
        /** What the standard cutout capture runs with, so the sizes are never read unset. */
        val DISABLED = FaceTrackingConfiguration(
            enabled = false,
            minFaceSizePx = FACE_TRACKING_MIN_FACE_SIZE_PX_DEFAULT,
            maxImageSizePx = FACE_TRACKING_MAX_IMAGE_SIZE_PX_DEFAULT,
        )
    }
}
