package com.simprints.infra.config.store.local.migrations

import androidx.datastore.core.DataMigration
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber
import javax.inject.Inject

class ProjectConfigFaceEmptyVersionMigration @Inject constructor() : DataMigration<ProtoProjectConfiguration> {
    override suspend fun cleanUp() {
        Simber.i("Migration of project configuration face bio sdk empty version is done", tag = MIGRATION)
    }

    override suspend fun shouldMigrate(currentData: ProtoProjectConfiguration) = with(currentData) {
        hasFace() && face.rankOne.version.isEmpty()
    }

    override suspend fun migrate(currentData: ProtoProjectConfiguration): ProtoProjectConfiguration {
        Simber.i("Start migration of project configuration face bio sdk empty version ", tag = MIGRATION)

        val faceProto = currentData.face.toBuilder()

        // 1- set version to 1.23
        faceProto.rankOne = faceProto.rankOne
            .toBuilder()
            .setVersion(ROC_V1_VERSION)
            .build()

        return currentData.toBuilder().setFace(faceProto).build()
    }

    companion object {
        const val ROC_V1_VERSION = "1.23"
    }
}
