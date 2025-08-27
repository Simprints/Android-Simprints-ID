package com.simprints.infra.enrolment.records.repository.domain.models

import com.google.common.truth.Truth
import com.simprints.core.domain.sample.SampleIdentifier
import org.junit.Test

class FingerIdentifierTest {
    @Test
    fun `should map correctly the Finger enums`() {
        val mapping = mapOf(
            SampleIdentifier.LEFT_THUMB to FingerIdentifier.LEFT_THUMB,
            SampleIdentifier.LEFT_INDEX_FINGER to FingerIdentifier.LEFT_INDEX_FINGER,
            SampleIdentifier.LEFT_3RD_FINGER to FingerIdentifier.LEFT_3RD_FINGER,
            SampleIdentifier.LEFT_4TH_FINGER to FingerIdentifier.LEFT_4TH_FINGER,
            SampleIdentifier.LEFT_5TH_FINGER to FingerIdentifier.LEFT_5TH_FINGER,
            SampleIdentifier.RIGHT_THUMB to FingerIdentifier.RIGHT_THUMB,
            SampleIdentifier.RIGHT_INDEX_FINGER to FingerIdentifier.RIGHT_INDEX_FINGER,
            SampleIdentifier.RIGHT_3RD_FINGER to FingerIdentifier.RIGHT_3RD_FINGER,
            SampleIdentifier.RIGHT_4TH_FINGER to FingerIdentifier.RIGHT_4TH_FINGER,
            SampleIdentifier.RIGHT_5TH_FINGER to FingerIdentifier.RIGHT_5TH_FINGER,
        )

        mapping.forEach {
            Truth.assertThat(it.key.fromModuleApiToDomain()).isEqualTo(it.value)
            Truth.assertThat(it.value.fromDomainToModuleApi()).isEqualTo(it.key)
        }
    }
}
