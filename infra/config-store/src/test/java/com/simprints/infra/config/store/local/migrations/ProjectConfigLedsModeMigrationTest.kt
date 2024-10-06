package com.simprints.infra.config.store.local.migrations

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.local.models.ProtoFingerprintConfiguration
import com.simprints.infra.config.store.local.models.ProtoFingerprintConfiguration.ProtoFingerprintSdkConfiguration
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import com.simprints.infra.config.store.local.models.ProtoVero2Configuration
import io.mockk.mockk
import io.mockk.every
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test



class ProjectConfigLedsModeMigrationTest {

    private lateinit var migration: ProjectConfigLedsModeMigration
    private lateinit var mockProtoConfig: ProtoProjectConfiguration
    private lateinit var mockFingerprint: ProtoFingerprintConfiguration
    private lateinit var mockSecugenSimMatcher: ProtoFingerprintSdkConfiguration
    private lateinit var mockVero2: ProtoVero2Configuration

    @Before
    fun setup() {
        // Common setup for mocks
        migration = ProjectConfigLedsModeMigration()

        mockProtoConfig = mockk(relaxed = true)
        mockFingerprint = mockk(relaxed = true)
        mockSecugenSimMatcher = mockk(relaxed = true)
        mockVero2 = mockk(relaxed = true)

        every { mockProtoConfig.fingerprint } returns mockFingerprint
        every { mockFingerprint.secugenSimMatcher } returns mockSecugenSimMatcher
        every { mockSecugenSimMatcher.vero2 } returns mockVero2
    }

    @Test
    fun `test displayLiveFeedback is enabled, then shouldMigrate should return true`() = runBlocking {
        // Given
        val mockVero2 = mockk<ProtoVero2Configuration>(relaxed = true)
        every { mockProtoConfig.fingerprint.secugenSimMatcher.vero2 } returns mockVero2
        every { mockVero2.displayLiveFeedback } returns true

        // When
        val result = migration.shouldMigrate(mockProtoConfig)

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `test displayLiveFeedback is disabled, then shouldMigrate should return false`() = runBlocking {
        // Given
        val mockVero2 = mockk<ProtoVero2Configuration>(relaxed = true)
        every { mockProtoConfig.fingerprint.secugenSimMatcher.vero2 } returns mockVero2
        every { mockVero2.displayLiveFeedback } returns false

        // When
        val result = migration.shouldMigrate(mockProtoConfig)

        // Then
        assertThat(result).isFalse()
    }
//Todo add the actual migration test case
}
