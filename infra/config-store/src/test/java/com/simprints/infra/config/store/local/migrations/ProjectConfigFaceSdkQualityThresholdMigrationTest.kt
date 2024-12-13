package com.simprints.infra.config.store.local.migrations

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.local.models.ProtoFaceConfiguration
import com.simprints.infra.config.store.local.models.ProtoFaceConfiguration.ProtoFaceSdkConfiguration
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ProjectConfigFaceSdkQualityThresholdMigrationTest {
    @Test
    fun `should migrate if face has quality threshold`() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .setFace(
                ProtoFaceConfiguration
                    .newBuilder()
                    .setRankOne(
                        ProtoFaceSdkConfiguration
                            .newBuilder()
                            .setQualityThresholdPrecise(0f)
                            .setQualityThreshold(1)
                            .build(),
                    ).build(),
            ).build()

        val shouldMigrate = ProjectConfigFaceSdkQualityThresholdMigration().shouldMigrate(currentData)

        assertThat(shouldMigrate).isTrue()
    }

    @Test
    fun `should not migrate if face has quality threshold`() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .setFace(
                ProtoFaceConfiguration
                    .newBuilder()
                    .setRankOne(
                        ProtoFaceSdkConfiguration
                            .newBuilder()
                            .setQualityThreshold(0)
                            .build(),
                    ).build(),
            ).build()

        val shouldMigrate = ProjectConfigFaceSdkQualityThresholdMigration().shouldMigrate(currentData)

        assertThat(shouldMigrate).isFalse()
    }

    @Test
    fun `migration converts quality threshold to float`() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .setFace(
                ProtoFaceConfiguration
                    .newBuilder()
                    .setRankOne(
                        ProtoFaceSdkConfiguration
                            .newBuilder()
                            .setQualityThreshold(1)
                            .setQualityThresholdPrecise(0f)
                            .build(),
                    ).build(),
            ).build()

        val migrated = ProjectConfigFaceSdkQualityThresholdMigration().migrate(currentData)

        assertThat(migrated.face.rankOne.qualityThreshold).isEqualTo(0)
        assertThat(migrated.face.rankOne.qualityThresholdPrecise).isEqualTo(1.0f)
    }
}
