package com.simprints.infra.config.store.local.migrations

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.local.models.ProtoFaceConfiguration
import com.simprints.infra.config.store.local.models.ProtoFaceConfiguration.ProtoFaceSdkConfiguration
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ProjectConfigFaceEmptyVersionMigrationTest {
    @Test
    fun `should not migrate if doesn't have face`() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .build()

        val shouldMigrate = ProjectConfigFaceEmptyVersionMigration().shouldMigrate(currentData)

        assertThat(shouldMigrate).isFalse()
    }

    @Test
    fun `should migrate if face has empty version`() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .setFace(
                ProtoFaceConfiguration
                    .newBuilder()
                    .setRankOne(
                        ProtoFaceSdkConfiguration
                            .newBuilder()
                            .build(),
                    ).build(),
            ).build()

        val shouldMigrate = ProjectConfigFaceEmptyVersionMigration().shouldMigrate(currentData)

        assertThat(shouldMigrate).isTrue()
    }

    @Test
    fun `should not migrate if face has version`() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .setFace(
                ProtoFaceConfiguration
                    .newBuilder()
                    .setRankOne(
                        ProtoFaceSdkConfiguration
                            .newBuilder()
                            .setVersion("1.23")
                            .build(),
                    ).build(),
            ).build()

        val shouldMigrate = ProjectConfigFaceEmptyVersionMigration().shouldMigrate(currentData)

        assertThat(shouldMigrate).isFalse()
    }

    @Test
    fun `migration converts empty version to ROC1`() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .setFace(
                ProtoFaceConfiguration
                    .newBuilder()
                    .setRankOne(
                        ProtoFaceSdkConfiguration
                            .newBuilder()
                            .build(),
                    ).build(),
            ).build()

        val migrated = ProjectConfigFaceEmptyVersionMigration().migrate(currentData)

        assertThat(migrated.face.rankOne.version).isEqualTo(ProjectConfigFaceEmptyVersionMigration.ROC_V1_VERSION)
    }
}
