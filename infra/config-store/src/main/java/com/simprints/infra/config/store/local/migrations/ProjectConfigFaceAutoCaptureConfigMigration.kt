package com.simprints.infra.config.store.local.migrations

import androidx.datastore.core.DataMigration
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber
import org.json.JSONException
import org.json.JSONObject
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
            JSONObject(currentData.customJson).optBoolean("faceAutoCaptureEnabled")
        } catch (e: JSONException) {
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
