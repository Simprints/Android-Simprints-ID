package com.simprints.face.capture.usecases

import com.simprints.face.capture.models.FaceTrackingConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.experimental
import javax.inject.Inject

internal class GetFaceTrackingConfigurationUseCase @Inject constructor() {
    operator fun invoke(projectConfiguration: ProjectConfiguration): FaceTrackingConfiguration =
        projectConfiguration.experimental().let { experimental ->
            val minFaceSizePx = experimental.faceTrackingMinFaceSizePx
            FaceTrackingConfiguration(
                enabled = experimental.faceTrackingCaptureEnabled,
                minFaceSizePx = minFaceSizePx,
                // A cap under the floor would scale every accepted face back below it, leaving
                // nothing capturable at all, so the two are kept in order however they are configured
                maxImageSizePx = experimental.faceTrackingMaxImageSizePx.coerceAtLeast(minFaceSizePx),
            )
        }
}
