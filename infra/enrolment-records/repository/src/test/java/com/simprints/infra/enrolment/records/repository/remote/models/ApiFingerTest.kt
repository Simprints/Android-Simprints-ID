package com.simprints.infra.enrolment.records.repository.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.common.TemplateIdentifier
import org.junit.Test

class ApiFingerTest {
    @Test
    fun `should map correctly the Finger enums`() {
        val mapping = mapOf(
            TemplateIdentifier.LEFT_THUMB to ApiFinger.LEFT_THUMB,
            TemplateIdentifier.LEFT_INDEX_FINGER to ApiFinger.LEFT_INDEX_FINGER,
            TemplateIdentifier.LEFT_3RD_FINGER to ApiFinger.LEFT_3RD_FINGER,
            TemplateIdentifier.LEFT_4TH_FINGER to ApiFinger.LEFT_4TH_FINGER,
            TemplateIdentifier.LEFT_5TH_FINGER to ApiFinger.LEFT_5TH_FINGER,
            TemplateIdentifier.RIGHT_THUMB to ApiFinger.RIGHT_THUMB,
            TemplateIdentifier.RIGHT_INDEX_FINGER to ApiFinger.RIGHT_INDEX_FINGER,
            TemplateIdentifier.RIGHT_3RD_FINGER to ApiFinger.RIGHT_3RD_FINGER,
            TemplateIdentifier.RIGHT_4TH_FINGER to ApiFinger.RIGHT_4TH_FINGER,
            TemplateIdentifier.RIGHT_5TH_FINGER to ApiFinger.RIGHT_5TH_FINGER,
        )

        mapping.forEach {
            assertThat(it.key.toApi()).isEqualTo(it.value)
        }
    }
}
