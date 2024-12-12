package com.simprints.fingerprint.capture.usecase

import com.simprints.core.domain.fingerprint.IFingerIdentifier
import javax.inject.Inject

internal class GetNextFingerToAddUseCase @Inject constructor() {
    operator fun invoke(existingFingers: List<IFingerIdentifier>): IFingerIdentifier? =
        DEFAULT_PRIORITY.minus(existingFingers.toSet()).firstOrNull()

    companion object {
        private val DEFAULT_PRIORITY = listOf(
            IFingerIdentifier.LEFT_THUMB,
            IFingerIdentifier.LEFT_INDEX_FINGER,
            IFingerIdentifier.RIGHT_THUMB,
            IFingerIdentifier.RIGHT_INDEX_FINGER,
            IFingerIdentifier.LEFT_3RD_FINGER,
            IFingerIdentifier.RIGHT_3RD_FINGER,
            IFingerIdentifier.LEFT_4TH_FINGER,
            IFingerIdentifier.RIGHT_4TH_FINGER,
            IFingerIdentifier.LEFT_5TH_FINGER,
            IFingerIdentifier.RIGHT_5TH_FINGER,
        )
    }
}
