package com.simprints.infra.config.store.local.migrations

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.local.models.ProtoFingerprintConfiguration
import com.simprints.infra.config.store.local.models.ProtoFingerprintConfiguration.ProtoFingerprintSdkConfiguration
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import com.simprints.infra.config.store.local.models.ProtoVero2Configuration
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
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
    fun `test doesn't have fingerprint, then shouldMigrate should return false`() = runBlocking {
        // Given
        every { mockProtoConfig.fingerprint } returns null

        // When
        val result = migration.shouldMigrate(mockProtoConfig)

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `test displayLiveFeedback is enabled, then shouldMigrate should return true`() = runBlocking {
        // Given
        every { mockProtoConfig.hasFingerprint() } returns true
        every { mockProtoConfig.fingerprint } returns mockFingerprint
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
        every { mockProtoConfig.hasFingerprint() } returns true
        every { mockProtoConfig.fingerprint } returns mockFingerprint
        val mockVero2 = mockk<ProtoVero2Configuration>(relaxed = true)
        every { mockProtoConfig.fingerprint.secugenSimMatcher.vero2 } returns mockVero2
        every { mockVero2.displayLiveFeedback } returns false

        // When
        val result = migration.shouldMigrate(mockProtoConfig)

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `test migration converts display live feedback from boolean to enum LIVE_QUALITY_FEEDBACK `() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .setFingerprint(
                ProtoFingerprintConfiguration
                    .newBuilder()
                    .setSecugenSimMatcher(
                        ProtoFingerprintSdkConfiguration
                            .newBuilder()
                            .setVero2(
                                ProtoVero2Configuration
                                    .newBuilder()
                                    .setDisplayLiveFeedback(true)
                                    .build(),
                            ),
                    ).build(),
            ).build()

        val migrated = migration.migrate(currentData)

        assertThat(migrated.fingerprint.secugenSimMatcher.vero2.displayLiveFeedback).isFalse()
        assertThat(migrated.fingerprint.secugenSimMatcher.vero2.ledsMode).isEqualTo(
            ProtoVero2Configuration.LedsMode.LIVE_QUALITY_FEEDBACK,
        )
    }
}
