package com.simprints.infra.eventsync.event.remote

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import org.junit.Test

class ApiExternalCredentialTypeTest {
    @Test
    fun `api type to domain mapping matches all supported credentials`() {
        val cases = mapOf(
            ApiExternalCredentialType.NHIS_CARD to ExternalCredentialType.NHISCard,
            ApiExternalCredentialType.GHANA_CARD to ExternalCredentialType.GhanaIdCard,
            ApiExternalCredentialType.QR_CODE to ExternalCredentialType.QRCode,
            ApiExternalCredentialType.FAYDA_CARD to ExternalCredentialType.FaydaCard,
        )

        cases.forEach { (apiType, expectedDomainType) ->
            assertThat(apiType.toDomain()).isEqualTo(expectedDomainType)
        }
    }

    @Test
    fun `domain type to api mapping matches all supported credentials`() {
        val cases = mapOf(
            ExternalCredentialType.NHISCard to ApiExternalCredentialType.NHIS_CARD,
            ExternalCredentialType.GhanaIdCard to ApiExternalCredentialType.GHANA_CARD,
            ExternalCredentialType.QRCode to ApiExternalCredentialType.QR_CODE,
            ExternalCredentialType.FaydaCard to ApiExternalCredentialType.FAYDA_CARD,
        )

        cases.forEach { (domainType, expectedApiType) ->
            assertThat(domainType.fromDomainToApi()).isEqualTo(expectedApiType)
        }
    }
}
