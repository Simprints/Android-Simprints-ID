package com.simprints.infra.config.store.local.migrations

import androidx.datastore.core.DataMigration
import com.simprints.infra.config.store.local.models.ProtoFingerprintConfiguration.VeroGeneration.VERO_1
import com.simprints.infra.config.store.local.models.ProtoFingerprintConfiguration.VeroGeneration.VERO_2
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import com.simprints.infra.config.store.local.models.ProtoVero1Configuration
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber
import javax.inject.Inject

/**
 * Can be removed once all the devices have been updated to 2023.3.0
 */
internal class ProjectConfigQualityThresholdMigration @Inject constructor() : DataMigration<ProtoProjectConfiguration> {
    override suspend fun cleanUp() {
        Simber.i("Migration of project configuration quality threshold is done", tag = MIGRATION)
    }

    override suspend fun migrate(currentData: ProtoProjectConfiguration): ProtoProjectConfiguration {
        Simber.i("Start migration of project configuration to Datastore", tag = MIGRATION)
        val qualityThreshold = currentData.fingerprint.qualityThreshold
        val fingerprintProto = currentData.fingerprint.toBuilder()
        if (currentData.fingerprint.allowedVeroGenerationsList.contains(VERO_1)) {
            fingerprintProto.vero1 = ProtoVero1Configuration
                .newBuilder()
                .setQualityThreshold(qualityThreshold)
                .build()
        }
        if (currentData.fingerprint.allowedVeroGenerationsList.contains(VERO_2)) {
            fingerprintProto.vero2 = fingerprintProto
                .vero2
                .toBuilder()
                .setQualityThreshold(qualityThreshold)
                .build()
        }
        fingerprintProto.qualityThreshold = 0
        return currentData.toBuilder().setFingerprint(fingerprintProto).build()
    }

    override suspend fun shouldMigrate(currentData: ProtoProjectConfiguration): Boolean =
        currentData.hasFingerprint() && currentData.fingerprint.qualityThreshold != 0
}
