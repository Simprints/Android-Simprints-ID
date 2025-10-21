package com.simprints.feature.externalcredential.screens.search.model

import com.google.common.truth.Truth.*
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.Timestamp
import org.junit.Test

class ScannedCredentialTest {
    private val testUuid = "testUuid"
    private val subjectId = "subjectId"

    @Test
    fun `toExternalCredential maps fields correctly`() {
        val tokenizedValue = "tokenizedValue".asTokenizableEncrypted()
        val scannedCredential = ScannedCredential(
            credentialScanId = testUuid,
            credential = tokenizedValue,
            credentialType = ExternalCredentialType.NHISCard,
            documentImagePath = null,
            zoomedCredentialImagePath = null,
            credentialBoundingBox = null,
            scanStartTime = Timestamp(1L),
            scanEndTime = Timestamp(2L),
            scannedValue = "".asTokenizableRaw(),
        )

        val result = scannedCredential.toExternalCredential(subjectId)

        assertThat(result.id).isEqualTo(testUuid)
        assertThat(result.value).isEqualTo(tokenizedValue)
        assertThat(result.subjectId).isEqualTo(subjectId)
        assertThat(result.type).isEqualTo(ExternalCredentialType.NHISCard)
    }
}
