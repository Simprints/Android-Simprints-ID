package com.simprints.infra.config.store.local.migrations

import androidx.datastore.core.DataMigration
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import com.simprints.infra.logging.Simber
import javax.inject.Inject

/**
 * Can be removed once all the devices have been updated to 2024.2.1
 */
internal class ProjectConfigMatchingModalitiesMigration @Inject constructor(
) : DataMigration<ProtoProjectConfiguration> {

    override suspend fun shouldMigrate(currentData: ProtoProjectConfiguration): Boolean =
        currentData.general.matchingModalitiesList.isEmpty()

    override suspend fun migrate(currentData: ProtoProjectConfiguration): ProtoProjectConfiguration {
        Simber.i("Start migration of matching modalities")

        return currentData.toBuilder()
            .setGeneral(
                currentData.general.toBuilder()
                    .addAllMatchingModalities(currentData.general.modalitiesList)
                    .build()
            )
            .build()
    }

    override suspend fun cleanUp() {
        Simber.i("Migration of matching modalities done")
    }
}