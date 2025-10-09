package com.simprints.feature.externalcredential.ext

import android.content.res.Resources
import com.google.common.truth.Truth.*
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import io.mockk.*
import org.junit.Test
import com.simprints.infra.resources.R as IDR

class ResourceExtTest {
    private val mockResources = mockk<Resources>(relaxed = true)

    @Test
    fun `getCredentialTypeRes returns correct resource ids`() {
        assertThat(mockResources.getCredentialTypeRes(ExternalCredentialType.NHISCard))
            .isEqualTo(IDR.string.mfid_type_nhis_card)
        assertThat(mockResources.getCredentialTypeRes(ExternalCredentialType.GhanaIdCard))
            .isEqualTo(IDR.string.mfid_type_ghana_id_card)
        assertThat(mockResources.getCredentialTypeRes(ExternalCredentialType.QRCode))
            .isEqualTo(IDR.string.mfid_type_qr_code)
        assertThat(mockResources.getCredentialTypeRes(null))
            .isEqualTo(IDR.string.mfid_type_any_document)
    }

    @Test
    fun `getCredentialFieldTitle returns correct resource ids for each type`() {
        every { mockResources.getString(any()) } returns ""
        mockResources.getCredentialFieldTitle(ExternalCredentialType.NHISCard)
        verify { mockResources.getString(IDR.string.mfid_nhis_card_credential_field) }
        mockResources.getCredentialFieldTitle(ExternalCredentialType.GhanaIdCard)
        verify { mockResources.getString(IDR.string.mfid_ghana_id_credential_field) }
        mockResources.getCredentialFieldTitle(ExternalCredentialType.QRCode)
        verify { mockResources.getString(IDR.string.mfid_qr_credential_field) }
    }

    @Test
    fun `getCredentialTypeString calls getString with correct resource id`() {
        val expected = "expected"
        every { mockResources.getString(any()) } returns expected
        val result = mockResources.getCredentialTypeString(ExternalCredentialType.NHISCard)
        verify { mockResources.getString(IDR.string.mfid_type_nhis_card) }
        assertThat(result).isEqualTo(expected)
    }
}
