package com.simprints.infra.config.store.local.migrations

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.local.models.ProtoFingerprintConfiguration
import com.simprints.infra.config.store.local.models.ProtoFingerprintConfiguration.VeroGeneration.VERO_1
import com.simprints.infra.config.store.local.models.ProtoFingerprintConfiguration.VeroGeneration.VERO_2
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import com.simprints.infra.config.store.local.models.ProtoVero1Configuration
import com.simprints.infra.config.store.local.models.ProtoVero2Configuration
import com.simprints.infra.config.store.local.models.ProtoVero2Configuration.CaptureStrategy.SECUGEN_ISO_500_DPI
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProjectConfigQualityThresholdMigrationTest {
    private val projectConfigQualityThresholdMigration = ProjectConfigQualityThresholdMigration()

    @Test
    fun `shouldMigrate should return false if the current data doesn't have a fingerprint field`() = runTest {
        val proto = ProtoProjectConfiguration.newBuilder().build()
        val shouldMigrate = projectConfigQualityThresholdMigration.shouldMigrate(proto)
        assertThat(shouldMigrate).isFalse()
    }

    @Test
    fun `shouldMigrate should return false if the current data has an empty quality threshold for fingerprint`() = runTest {
        val proto = ProtoProjectConfiguration
            .newBuilder()
            .setFingerprint(
                ProtoFingerprintConfiguration.newBuilder().setQualityThreshold(0).build(),
            ).build()
        val shouldMigrate = projectConfigQualityThresholdMigration.shouldMigrate(proto)
        assertThat(shouldMigrate).isFalse()
    }

    @Test
    fun `shouldMigrate should return true if the current data has a non empty quality threshold for fingerprint`() = runTest {
        val proto = ProtoProjectConfiguration
            .newBuilder()
            .setFingerprint(
                ProtoFingerprintConfiguration.newBuilder().setQualityThreshold(10).build(),
            ).build()
        val shouldMigrate = projectConfigQualityThresholdMigration.shouldMigrate(proto)
        assertThat(shouldMigrate).isTrue()
    }

    @Test
    fun `migrate should migrate both vero1 and vero2`() = runTest {
        val proto = ProtoProjectConfiguration
            .newBuilder()
            .setFingerprint(
                ProtoFingerprintConfiguration
                    .newBuilder()
                    .setQualityThreshold(10)
                    .addAllAllowedVeroGenerations(listOf(VERO_1, VERO_2))
                    .setVero2(
                        ProtoVero2Configuration
                            .newBuilder()
                            .setCaptureStrategy(SECUGEN_ISO_500_DPI)
                            .build(),
                    ).build(),
            ).build()
        val expectedProto = ProtoProjectConfiguration
            .newBuilder()
            .setFingerprint(
                ProtoFingerprintConfiguration
                    .newBuilder()
                    .setQualityThreshold(0)
                    .addAllAllowedVeroGenerations(listOf(VERO_1, VERO_2))
                    .setVero2(
                        ProtoVero2Configuration
                            .newBuilder()
                            .setQualityThreshold(10)
                            .setCaptureStrategy(SECUGEN_ISO_500_DPI)
                            .build(),
                    ).setVero1(
                        ProtoVero1Configuration
                            .newBuilder()
                            .setQualityThreshold(10)
                            .build(),
                    ).build(),
            ).build()

        val migratedProto = projectConfigQualityThresholdMigration.migrate(proto)
        assertThat(migratedProto).isEqualTo(expectedProto)
    }
}
