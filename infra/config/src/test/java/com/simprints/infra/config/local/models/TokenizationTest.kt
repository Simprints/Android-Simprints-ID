package com.simprints.infra.config.local.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.testtools.project
import com.simprints.infra.config.testtools.tokenizationKeysLocal
import org.junit.Test

class TokenizationTest {
    @Test
    fun `remote tokenization keys should map correctly to domain tokenization keys`() {
        val remoteMap = tokenizationKeysLocal
        val domainMap = project.tokenizationKeys
        assertThat(remoteMap.mapTokenizationKeysToDomain()).isEqualTo(domainMap)
    }

    @Test
    fun `domain tokenization keys should map correctly to remote tokenization keys`() {
        assertThat(project.tokenizationKeys.mapTokenizationKeysToLocal()).isEqualTo(tokenizationKeysLocal)
    }
}