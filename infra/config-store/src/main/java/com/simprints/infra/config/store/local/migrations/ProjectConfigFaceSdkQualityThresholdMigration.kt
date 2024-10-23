package com.simprints.infra.config.store.local.migrations

import androidx.datastore.core.DataMigration
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import com.simprints.infra.logging.Simber
import javax.inject.Inject

/**
 * Can be removed once all the devices have been updated to 2024.2.1
 */
internal class ProjectConfigFaceSdkQualityThresholdMigration @Inject constructor(
) : DataMigration<ProtoProjectConfiguration> {

    override suspend fun cleanUp() {
        Simber.i("Migration of SDK quality to float numbers done")
    }

    override suspend fun shouldMigrate(currentData: ProtoProjectConfiguration): Boolean =
        currentData.face?.rankOne?.qualityThreshold?.let { it > 0 } == true


    override suspend fun migrate(currentData: ProtoProjectConfiguration): ProtoProjectConfiguration {
        Simber.i("Start migration of SDK quality to float numbers")

        val faceProto = currentData.face.toBuilder()
        val rankOneProto = faceProto.rankOne.toBuilder()
            .setQualityThresholdPrecise(faceProto.rankOne.qualityThreshold.toFloat())
            .setQualityThreshold(0)
            .build()

        return currentData.toBuilder()
            .setFace(faceProto.setRankOne(rankOneProto).build())
            .build()
    }

}
