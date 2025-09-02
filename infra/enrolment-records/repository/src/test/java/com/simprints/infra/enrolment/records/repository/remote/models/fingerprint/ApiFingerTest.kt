package com.simprints.infra.enrolment.records.repository.remote.models.fingerprint

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.sample.SampleIdentifier
import org.junit.Test

class ApiFingerTest {
    @Test
    fun `should map correctly the Finger enums`() {
        val mapping = mapOf(
            SampleIdentifier.LEFT_THUMB to ApiFinger.LEFT_THUMB,
            SampleIdentifier.LEFT_INDEX_FINGER to ApiFinger.LEFT_INDEX_FINGER,
            SampleIdentifier.LEFT_3RD_FINGER to ApiFinger.LEFT_3RD_FINGER,
            SampleIdentifier.LEFT_4TH_FINGER to ApiFinger.LEFT_4TH_FINGER,
            SampleIdentifier.LEFT_5TH_FINGER to ApiFinger.LEFT_5TH_FINGER,
            SampleIdentifier.RIGHT_THUMB to ApiFinger.RIGHT_THUMB,
            SampleIdentifier.RIGHT_INDEX_FINGER to ApiFinger.RIGHT_INDEX_FINGER,
            SampleIdentifier.RIGHT_3RD_FINGER to ApiFinger.RIGHT_3RD_FINGER,
            SampleIdentifier.RIGHT_4TH_FINGER to ApiFinger.RIGHT_4TH_FINGER,
            SampleIdentifier.RIGHT_5TH_FINGER to ApiFinger.RIGHT_5TH_FINGER,
        )

        mapping.forEach {
            assertThat(it.key.toApi()).isEqualTo(it.value)
        }
    }
}
