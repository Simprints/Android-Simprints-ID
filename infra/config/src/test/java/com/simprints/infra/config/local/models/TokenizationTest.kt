package com.simprints.infra.config.local.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.testtools.project
import com.simprints.infra.config.testtools.tokenizationItem
import com.simprints.infra.config.testtools.tokenizationKeyData
import com.simprints.infra.config.testtools.tokenizationKeys
import org.junit.Test

class TokenizationTest {
    @Test
    fun `remote tokenization map should map correctly to domain tokenization map`() {
        val remoteMap = tokenizationKeys
        val domainMap = project.tokenizationKeys
        assertThat(remoteMap.asTokenizationKeysMap()).isEqualTo(domainMap)
    }

    @Test
    fun `tokenization item should map correctly to tokenization data`() {
        assertThat(tokenizationItem.toTokenizationKeyData()).isEqualTo(tokenizationKeyData)
    }

    @Test
    fun `tokenization data should map correctly to tokenization item`() {
        assertThat(TokenizationItem.fromTokenizationKeyData(tokenizationKeyData))
            .isEqualTo(tokenizationItem)
    }
}