package com.simprints.feature.externalcredential.screens.search.model

import com.google.common.truth.Truth.*
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import io.mockk.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

class ScannedCredentialTest {
    private val testUuid = "testUuid"
    private val subjectId = "subjectId"

    @Before
    fun setup() {
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns testUuid
    }

    @Test
    fun `toExternalCredential maps fields correctly`() {
        val tokenizedValue = "tokenizedValue".asTokenizableEncrypted()
        val scannedCredential = mockk<ScannedCredential> {
            every { credential } returns tokenizedValue
            every { credentialType } returns ExternalCredentialType.NHISCard
        }
        val result = scannedCredential.toExternalCredential(subjectId)

        assertThat(result.id).isEqualTo(testUuid)
        assertThat(result.value).isEqualTo(tokenizedValue)
        assertThat(result.subjectId).isEqualTo(subjectId)
        assertThat(result.type).isEqualTo(ExternalCredentialType.NHISCard)
    }
}
