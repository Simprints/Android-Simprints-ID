package com.simprints.fingerprint.capture.usecase

import com.simprints.core.domain.sample.SampleIdentifier
import javax.inject.Inject

internal class GetNextFingerToAddUseCase @Inject constructor() {
    operator fun invoke(existingFingers: List<SampleIdentifier>): SampleIdentifier? =
        DEFAULT_PRIORITY.minus(existingFingers.toSet()).firstOrNull()

    companion object {
        private val DEFAULT_PRIORITY = listOf(
            SampleIdentifier.LEFT_THUMB,
            SampleIdentifier.LEFT_INDEX_FINGER,
            SampleIdentifier.RIGHT_THUMB,
            SampleIdentifier.RIGHT_INDEX_FINGER,
            SampleIdentifier.LEFT_3RD_FINGER,
            SampleIdentifier.RIGHT_3RD_FINGER,
            SampleIdentifier.LEFT_4TH_FINGER,
            SampleIdentifier.RIGHT_4TH_FINGER,
            SampleIdentifier.LEFT_5TH_FINGER,
            SampleIdentifier.RIGHT_5TH_FINGER,
        )
    }
}
