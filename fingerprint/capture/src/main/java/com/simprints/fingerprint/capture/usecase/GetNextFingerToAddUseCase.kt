package com.simprints.fingerprint.capture.usecase

import com.simprints.core.domain.common.TemplateIdentifier
import javax.inject.Inject

internal class GetNextFingerToAddUseCase @Inject constructor() {
    operator fun invoke(existingFingers: List<TemplateIdentifier>): TemplateIdentifier? =
        DEFAULT_PRIORITY.minus(existingFingers.toSet()).firstOrNull()

    companion object {
        private val DEFAULT_PRIORITY = listOf(
            TemplateIdentifier.LEFT_THUMB,
            TemplateIdentifier.LEFT_INDEX_FINGER,
            TemplateIdentifier.RIGHT_THUMB,
            TemplateIdentifier.RIGHT_INDEX_FINGER,
            TemplateIdentifier.LEFT_3RD_FINGER,
            TemplateIdentifier.RIGHT_3RD_FINGER,
            TemplateIdentifier.LEFT_4TH_FINGER,
            TemplateIdentifier.RIGHT_4TH_FINGER,
            TemplateIdentifier.LEFT_5TH_FINGER,
            TemplateIdentifier.RIGHT_5TH_FINGER,
        )
    }
}
