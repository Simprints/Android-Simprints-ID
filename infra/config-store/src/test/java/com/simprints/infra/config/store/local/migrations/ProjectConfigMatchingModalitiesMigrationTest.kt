package com.simprints.infra.config.store.local.migrations

import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import org.junit.Test
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.local.models.ProtoGeneralConfiguration
import com.simprints.infra.config.store.local.models.ProtoGeneralConfiguration.Modality.FACE
import com.simprints.infra.config.store.local.models.ProtoGeneralConfiguration.Modality.FINGERPRINT
import kotlinx.coroutines.test.runTest

class ProjectConfigMatchingModalitiesMigrationTest {

    @Test
    fun `should migrate if matching modalities is empty`() = runTest {
        // Given
        val currentData = ProtoProjectConfiguration.newBuilder()
            .setGeneral(
                ProtoGeneralConfiguration.newBuilder()
                    .clearMatchingModalities()
                    .build()
            ).build()

        // When
        val shouldMigrate = ProjectConfigMatchingModalitiesMigration().shouldMigrate(currentData)

        // Then
        assertThat(shouldMigrate).isTrue()
    }

    @Test
    fun `should not migrate if matching modalities is not empty`() = runTest {
        // Given
        val currentData = ProtoProjectConfiguration.newBuilder()
            .setGeneral(
                ProtoGeneralConfiguration.newBuilder()
                    .addMatchingModalities(FACE)
                    .build()
            ).build()

        // When
        val shouldMigrate = ProjectConfigMatchingModalitiesMigration().shouldMigrate(currentData)

        // Then
        assertThat(shouldMigrate).isFalse()
    }

    @Test
    fun `migration adds all modalities to matching modalities`() = runTest {
        // Given
        val currentData = ProtoProjectConfiguration.newBuilder()
            .setGeneral(
                ProtoGeneralConfiguration.newBuilder()
                    .addAllModalities(listOf(FACE, FINGERPRINT))
                    .clearMatchingModalities()
                    .build()
            ).build()

        // When
        val migratedData = ProjectConfigMatchingModalitiesMigration().migrate(currentData)

        // Then
        assertThat(migratedData.general.matchingModalitiesList).containsExactlyElementsIn(
            currentData.general.modalitiesList
        )
    }
}