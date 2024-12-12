package com.simprints.infra.config.store.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.ConsentConfiguration
import com.simprints.infra.config.store.testtools.apiConsentConfiguration
import com.simprints.infra.config.store.testtools.consentConfiguration
import org.junit.Test

class ApiConsentConfigurationTest {
    @Test
    fun `should map correctly the model`() {
        assertThat(apiConsentConfiguration.toDomain()).isEqualTo(consentConfiguration)
    }

    @Test
    fun `should map correctly the model when the prompts are missing`() {
        val apiConsentConfiguration = ApiConsentConfiguration(
            programName = "programName",
            organizationName = "organizationName",
            collectConsent = true,
            displaySimprintsLogo = false,
            allowParentalConsent = false,
            generalPrompt = null,
            parentalPrompt = null,
        )
        val consentConfiguration = ConsentConfiguration(
            programName = "programName",
            organizationName = "organizationName",
            collectConsent = true,
            displaySimprintsLogo = false,
            allowParentalConsent = false,
            generalPrompt = null,
            parentalPrompt = null,
        )
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
