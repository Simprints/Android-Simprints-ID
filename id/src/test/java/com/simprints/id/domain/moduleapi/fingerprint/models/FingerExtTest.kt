package com.simprints.id.domain.moduleapi.fingerprint.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.domain.models.Finger
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import org.junit.Test

class FingerExtTest {
    @Test
    fun `should map correctly the Finger enums`() {
        val mapping = mapOf(
            IFingerIdentifier.LEFT_THUMB to Finger.LEFT_THUMB,
            IFingerIdentifier.LEFT_INDEX_FINGER to Finger.LEFT_INDEX_FINGER,
            IFingerIdentifier.LEFT_3RD_FINGER to Finger.LEFT_3RD_FINGER,
            IFingerIdentifier.LEFT_4TH_FINGER to Finger.LEFT_4TH_FINGER,
            IFingerIdentifier.LEFT_5TH_FINGER to Finger.LEFT_5TH_FINGER,
            IFingerIdentifier.RIGHT_THUMB to Finger.RIGHT_THUMB,
            IFingerIdentifier.RIGHT_INDEX_FINGER to Finger.RIGHT_INDEX_FINGER,
            IFingerIdentifier.RIGHT_3RD_FINGER to Finger.RIGHT_3RD_FINGER,
            IFingerIdentifier.RIGHT_4TH_FINGER to Finger.RIGHT_4TH_FINGER,
            IFingerIdentifier.RIGHT_5TH_FINGER to Finger.RIGHT_5TH_FINGER,
        )

        mapping.forEach {
            assertThat(it.key.fromModuleApiToDomain()).isEqualTo(it.value)
            assertThat(it.value.fromDomainToModuleApi()).isEqualTo(it.key)
        }
    }
}
