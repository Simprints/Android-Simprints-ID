package com.simprints.infra.config.store.models

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.infra.config.store.exceptions.InvalidProtobufEnumException
import com.simprints.infra.config.store.local.models.ProtoExternalCredentialType
import com.simprints.infra.config.store.local.models.toDomain
import com.simprints.infra.config.store.local.models.toProto
import com.simprints.testtools.common.syntax.assertThrows

class ExternalCredentialTypeMapperTest {

    @Test
    fun `should map correctly the model to proto`() {
        val pairs = listOf(
            ExternalCredentialType.NHISCard to ProtoExternalCredentialType.NHIS_CARD,
            ExternalCredentialType.GhanaIdCard to ProtoExternalCredentialType.GHANA_ID_CARD,
            ExternalCredentialType.QRCode to ProtoExternalCredentialType.QR_CODE
        )

        pairs.forEach { (domain, proto) ->
            assertThat(proto).isEqualTo(domain.toProto())
        }
    }

    @Test
    fun `should map correctly the model from proto`() {
        val pairs = listOf(
            ProtoExternalCredentialType.NHIS_CARD to ExternalCredentialType.NHISCard,
            ProtoExternalCredentialType.GHANA_ID_CARD to ExternalCredentialType.GhanaIdCard,
            ProtoExternalCredentialType.QR_CODE to ExternalCredentialType.QRCode
        )

        pairs.forEach { (proto, domain) ->
            assertThat(domain).isEqualTo(proto.toDomain())
        }

        assertThrows<InvalidProtobufEnumException> {
            ProtoExternalCredentialType.UNRECOGNIZED.toDomain()
        }
    }
}
