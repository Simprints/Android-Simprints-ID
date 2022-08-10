package com.simprints.infra.config.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.domain.ConsentConfiguration
import com.simprints.infra.config.testtools.apiConsentConfiguration
import com.simprints.infra.config.testtools.consentConfiguration
import org.junit.Test

class ApiConsentConfigurationTest {

    @Test
    fun `should map correctly the model`() {
        assertThat(apiConsentConfiguration.toDomain()).isEqualTo(consentConfiguration)
    }

    @Test
    fun `should map correctly the ConsentEnrolmentVariant enums`() {
        val mapping = mapOf(
            ApiConsentConfiguration.ConsentEnrolmentVariant.STANDARD to ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
            ApiConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY to ConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
        }
    }
}
