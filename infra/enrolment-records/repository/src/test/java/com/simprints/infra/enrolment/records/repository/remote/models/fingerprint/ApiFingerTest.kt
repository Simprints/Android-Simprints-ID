package com.simprints.infra.enrolment.records.repository.remote.models.fingerprint

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import org.junit.Test

class ApiFingerTest {
    @Test
    fun `should map correctly the Finger enums`() {
        val mapping = mapOf(
            IFingerIdentifier.LEFT_THUMB to ApiFinger.LEFT_THUMB,
            IFingerIdentifier.LEFT_INDEX_FINGER to ApiFinger.LEFT_INDEX_FINGER,
            IFingerIdentifier.LEFT_3RD_FINGER to ApiFinger.LEFT_3RD_FINGER,
            IFingerIdentifier.LEFT_4TH_FINGER to ApiFinger.LEFT_4TH_FINGER,
            IFingerIdentifier.LEFT_5TH_FINGER to ApiFinger.LEFT_5TH_FINGER,
            IFingerIdentifier.RIGHT_THUMB to ApiFinger.RIGHT_THUMB,
            IFingerIdentifier.RIGHT_INDEX_FINGER to ApiFinger.RIGHT_INDEX_FINGER,
            IFingerIdentifier.RIGHT_3RD_FINGER to ApiFinger.RIGHT_3RD_FINGER,
            IFingerIdentifier.RIGHT_4TH_FINGER to ApiFinger.RIGHT_4TH_FINGER,
            IFingerIdentifier.RIGHT_5TH_FINGER to ApiFinger.RIGHT_5TH_FINGER,
        )

        mapping.forEach {
            assertThat(it.key.toApi()).isEqualTo(it.value)
        }
    }
}
