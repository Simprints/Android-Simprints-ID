package com.simprints.infra.config.store.local.migrations

import androidx.datastore.core.DataMigration
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.serialization.SimJson
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

/**
 * Can be removed once all the devices have been updated to 2026.3.0
 */
class ProjectConfigFaceAutoCaptureConfigMigration @Inject constructor() : DataMigration<ProtoProjectConfiguration> {
    override suspend fun cleanUp() {
        Simber.i("Migration of face auto-capture flag is complete", tag = MIGRATION)
    }

    override suspend fun shouldMigrate(currentData: ProtoProjectConfiguration) = with(currentData) {
        hasFace() && !face.hasIsAutoCapture() && hasCustomJson() && customJson.contains("faceAutoCaptureEnabled")
    }

    override suspend fun migrate(currentData: ProtoProjectConfiguration): ProtoProjectConfiguration {
        Simber.i("Start migration of face auto-capture flag", tag = MIGRATION)

        val isAutoCapture = try {
            SimJson
                .parseToJsonElement(currentData.customJson)
                .jsonObject["faceAutoCaptureEnabled"]
                ?.jsonPrimitive
                ?.booleanOrNull ?: false
        } catch (e: SerializationException) {
            Simber.e("Failed to parse custom config", e, tag = MIGRATION)
            false
        }

        return currentData
            .toBuilder()
            .setFace(
                currentData.face
                    .toBuilder()
                    .setIsAutoCapture(isAutoCapture)
                    .build(),
            ).build()
    }
}
