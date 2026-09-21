package com.simprints.face.capture.usecases

import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.experimental
import javax.inject.Inject

internal class IsFaceTrackingEnabledUseCase @Inject constructor() {
    operator fun invoke(projectConfiguration: ProjectConfiguration): Boolean =
        projectConfiguration.experimental().faceTrackingCaptureEnabled
}
