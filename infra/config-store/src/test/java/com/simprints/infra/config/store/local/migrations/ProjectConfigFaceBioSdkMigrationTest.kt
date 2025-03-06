package com.simprints.infra.config.store.local.migrations

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.local.models.ProtoAllowedAgeRange
import com.simprints.infra.config.store.local.models.ProtoFaceConfiguration
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import com.simprints.infra.config.store.testtools.protoDecisionPolicy
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ProjectConfigFaceBioSdkMigrationTest {
    @Test
    fun `should not migrate if doesn't have face`() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .build()
        val shouldMigrate = ProjectConfigFaceBioSdkMigration().shouldMigrate(currentData)

        assertThat(shouldMigrate).isFalse()
    }

    @Test
    fun `should migrate if face has no rankone config`() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .setFace(
                ProtoFaceConfiguration
                    .newBuilder()
                    .addAllowedSdks(ProtoFaceConfiguration.ProtoBioSdk.RANK_ONE)
                    .build(),
            ).build()

        val shouldMigrate = ProjectConfigFaceBioSdkMigration().shouldMigrate(currentData)

        assertThat(shouldMigrate).isTrue()
    }

    @Test
    fun `should migrate if face allowedSdks is empty `() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .setFace(
                ProtoFaceConfiguration
                    .newBuilder()
                    .setRankOne(
                        ProtoFaceConfiguration.ProtoFaceSdkConfiguration
                            .newBuilder()
                            .setNbOfImagesToCapture(5)
                            .setQualityThreshold(1)
                            .setImageSavingStrategy(ProtoFaceConfiguration.ImageSavingStrategy.NEVER)
                            .setDecisionPolicy(protoDecisionPolicy)
                            .build(),
                    ).build(),
            ).build()

        val shouldMigrate = ProjectConfigFaceBioSdkMigration().shouldMigrate(currentData)

        assertThat(shouldMigrate).isTrue()
    }

    @Test
    fun `should not migrate if face has rankone config and allowedSdks is not empty`() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .setFace(
                ProtoFaceConfiguration
                    .newBuilder()
                    .setRankOne(
                        ProtoFaceConfiguration.ProtoFaceSdkConfiguration.newBuilder().build(),
                    ).addAllowedSdks(ProtoFaceConfiguration.ProtoBioSdk.RANK_ONE)
                    .build(),
            ).build()
        val shouldMigrate = ProjectConfigFaceBioSdkMigration().shouldMigrate(currentData)

        assertThat(shouldMigrate).isFalse()
    }

    @Test
    fun `should create rankone with value of the old face configuration`() = runTest {
        val imageSavingStrategy = ProtoFaceConfiguration.ImageSavingStrategy.NEVER
        val nbOfImagesToCapture = 2
        val qualityThreshold = 1
        val decisionPolicy = protoDecisionPolicy
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .setFace(
                ProtoFaceConfiguration
                    .newBuilder()
                    .setNbOfImagesToCapture(nbOfImagesToCapture)
                    .setQualityThreshold(qualityThreshold)
                    .setImageSavingStrategy(imageSavingStrategy)
                    .setDecisionPolicy(decisionPolicy)
                    .build(),
            ).build()

        val migratedData =
            ProjectConfigFaceBioSdkMigration().migrate(currentData).face.rankOne

        assertThat(migratedData.decisionPolicy).isEqualTo(decisionPolicy)
        assertThat(migratedData.imageSavingStrategy).isEqualTo(imageSavingStrategy)
        assertThat(migratedData.nbOfImagesToCapture).isEqualTo(nbOfImagesToCapture)
        assertThat(migratedData.qualityThreshold).isEqualTo(qualityThreshold)
        assertThat(migratedData.version).isEmpty()
        assertThat(migratedData.allowedAgeRange).isEqualTo(ProtoAllowedAgeRange.getDefaultInstance())
    }
}
