package com.simprints.infra.config.store.local.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.ConsentConfiguration
import com.simprints.infra.config.store.testtools.consentConfiguration
import com.simprints.infra.config.store.testtools.protoConsentConfiguration
import org.junit.Test

class ConsentConfigurationTest {
    @Test
    fun `should map correctly the model`() {
        assertThat(protoConsentConfiguration.toDomain()).isEqualTo(consentConfiguration)
        assertThat(consentConfiguration.toProto()).isEqualTo(protoConsentConfiguration)
    }

    @Test
    fun `should map correctly the ConsentEnrolmentVariant enums`() {
        val mapping = mapOf(
            ProtoConsentConfiguration.ConsentEnrolmentVariant.STANDARD to ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
            ProtoConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY to ConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
            assertThat(it.value.toProto()).isEqualTo(it.key)
        }
    }
}
