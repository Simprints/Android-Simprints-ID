package com.simprints.infra.enrolment.records.repository.domain.models

import com.google.common.truth.Truth
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import org.junit.Test

class FingerIdentifierTest {
    @Test
    fun `should map correctly the Finger enums`() {
        val mapping = mapOf(
            IFingerIdentifier.LEFT_THUMB to FingerIdentifier.LEFT_THUMB,
            IFingerIdentifier.LEFT_INDEX_FINGER to FingerIdentifier.LEFT_INDEX_FINGER,
            IFingerIdentifier.LEFT_3RD_FINGER to FingerIdentifier.LEFT_3RD_FINGER,
            IFingerIdentifier.LEFT_4TH_FINGER to FingerIdentifier.LEFT_4TH_FINGER,
            IFingerIdentifier.LEFT_5TH_FINGER to FingerIdentifier.LEFT_5TH_FINGER,
            IFingerIdentifier.RIGHT_THUMB to FingerIdentifier.RIGHT_THUMB,
            IFingerIdentifier.RIGHT_INDEX_FINGER to FingerIdentifier.RIGHT_INDEX_FINGER,
            IFingerIdentifier.RIGHT_3RD_FINGER to FingerIdentifier.RIGHT_3RD_FINGER,
            IFingerIdentifier.RIGHT_4TH_FINGER to FingerIdentifier.RIGHT_4TH_FINGER,
            IFingerIdentifier.RIGHT_5TH_FINGER to FingerIdentifier.RIGHT_5TH_FINGER,
        )

        mapping.forEach {
            Truth.assertThat(it.key.fromModuleApiToDomain()).isEqualTo(it.value)
            Truth.assertThat(it.value.fromDomainToModuleApi()).isEqualTo(it.key)
        }
    }
}
