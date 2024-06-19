package com.simprints.infra.config.store.local.migrations

import androidx.datastore.core.DataMigration
import com.simprints.infra.config.store.local.models.ProtoFaceConfiguration
import com.simprints.infra.config.store.local.models.ProtoFaceConfiguration.ProtoFaceSdkConfiguration
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import com.simprints.infra.logging.Simber
import javax.inject.Inject

/**
 * Can be removed once all the devices have been updated to 2024.2.0
 */
class ProjectConfigFaceBioSdkMigration @Inject constructor() :
    DataMigration<ProtoProjectConfiguration> {
    override suspend fun cleanUp() {
        Simber.i("Migration of project configuration face bio sdk is done")
    }

    override suspend fun shouldMigrate(currentData: ProtoProjectConfiguration): Boolean =
        !currentData.face.hasRankOne()

    override suspend fun migrate(currentData: ProtoProjectConfiguration): ProtoProjectConfiguration {
        Simber.i("Start migration of project configuration from face to specific bio sdk")

        val face = currentData.face

        val faceProto = face.toBuilder()
            // 1 - move generic "face" config into "rankOne" config
            .addAllowedSdks(ProtoFaceConfiguration.ProtoBioSdk.RANK_ONE)
            .setRankOne(
                ProtoFaceSdkConfiguration.newBuilder()
                    .setNbOfImagesToCapture(face.nbOfImagesToCapture)
                    .setQualityThreshold(face.qualityThreshold)
                    .setImageSavingStrategy(face.imageSavingStrategy)
                    .setDecisionPolicy(face.decisionPolicy)
                    .build()
            )
            // 2 - remove generic config
            .clearNbOfImagesToCapture()
            .clearQualityThreshold()
            .clearImageSavingStrategy()
            .clearDecisionPolicy()

        return currentData.toBuilder().setFace(faceProto.build()).build()
    }
}
