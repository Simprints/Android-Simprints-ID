package com.simprints.infra.config.store.models

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.sample.SampleIdentifier
import org.junit.Test

class FingerExtTest {
    @Test
    fun `should map correctly the Finger enums`() {
        val mapping = mapOf(
            SampleIdentifier.LEFT_THUMB to Finger.LEFT_THUMB,
            SampleIdentifier.LEFT_INDEX_FINGER to Finger.LEFT_INDEX_FINGER,
            SampleIdentifier.LEFT_3RD_FINGER to Finger.LEFT_3RD_FINGER,
            SampleIdentifier.LEFT_4TH_FINGER to Finger.LEFT_4TH_FINGER,
            SampleIdentifier.LEFT_5TH_FINGER to Finger.LEFT_5TH_FINGER,
            SampleIdentifier.RIGHT_THUMB to Finger.RIGHT_THUMB,
            SampleIdentifier.RIGHT_INDEX_FINGER to Finger.RIGHT_INDEX_FINGER,
            SampleIdentifier.RIGHT_3RD_FINGER to Finger.RIGHT_3RD_FINGER,
            SampleIdentifier.RIGHT_4TH_FINGER to Finger.RIGHT_4TH_FINGER,
            SampleIdentifier.RIGHT_5TH_FINGER to Finger.RIGHT_5TH_FINGER,
        )

        mapping.forEach {
            assertThat(it.key.fromModuleApiToDomain()).isEqualTo(it.value)
            assertThat(it.value.toDomain()).isEqualTo(it.key)
        }
    }
}
