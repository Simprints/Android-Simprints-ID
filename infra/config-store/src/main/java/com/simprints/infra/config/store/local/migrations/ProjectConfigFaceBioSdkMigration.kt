package com.simprints.infra.config.store.local.migrations

import androidx.datastore.core.DataMigration
import com.simprints.infra.config.store.local.models.ProtoFaceConfiguration
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

    override suspend fun shouldMigrate(currentData: ProtoProjectConfiguration) =
        with(currentData.face) {
            !hasRankOne() || allowedSdksCount == 0
        }

    override suspend fun migrate(currentData: ProtoProjectConfiguration): ProtoProjectConfiguration {
        Simber.i("Start migration of project configuration face bio sdk to Datastore")

        val faceProto = currentData.face.toBuilder()

        // 1- add RANK_ONE to allowedSdks
        faceProto.addAllowedSdks(ProtoFaceConfiguration.ProtoBioSdk.RANK_ONE)

        // 2- move face config to face.rankOne
        faceProto.setRankOne(
            ProtoFaceConfiguration.ProtoFaceSdkConfiguration
                .newBuilder()
                .setNbOfImagesToCapture(faceProto.nbOfImagesToCapture)
                .setQualityThreshold(faceProto.qualityThreshold)
                .setImageSavingStrategy(faceProto.imageSavingStrategy)
                .setDecisionPolicy(faceProto.decisionPolicy)
                .setVersion("") // Empty version will be treated as 1.23
                // allowed age range and verification match threshold will be null as they are not present in the old face configuration

                .build()
        )
        return currentData.toBuilder().setFace(faceProto).build()
    }
}
