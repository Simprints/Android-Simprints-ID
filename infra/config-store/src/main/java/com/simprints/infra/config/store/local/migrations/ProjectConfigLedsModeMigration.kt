package com.simprints.infra.config.store.local.migrations

import androidx.datastore.core.DataMigration
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import com.simprints.infra.config.store.local.models.ProtoVero2Configuration.LedsMode.LIVE_QUALITY_FEEDBACK
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber
import javax.inject.Inject

/**
 * Can be removed once all the devices have been updated to 2023.4.0
 */
class ProjectConfigLedsModeMigration @Inject constructor() : DataMigration<ProtoProjectConfiguration> {
    override suspend fun cleanUp() {
        Simber.i("Migration of project configuration displayLiveFeedback to leds mode is done", tag = MIGRATION)
    }

    override suspend fun shouldMigrate(currentData: ProtoProjectConfiguration) = with(currentData) {
        hasFingerprint() && fingerprint.secugenSimMatcher.vero2.displayLiveFeedback
    }

    override suspend fun migrate(currentData: ProtoProjectConfiguration): ProtoProjectConfiguration {
        Simber.i("Start migration of project configuration displayLiveFeedback to leds mode ", tag = MIGRATION)

        val fingerprintProto = currentData.fingerprint.toBuilder()

        val oldVero2Configuration = fingerprintProto.secugenSimMatcher.vero2

        val newVero2Configuration =
            oldVero2Configuration
                .toBuilder()
                .setLedsMode(LIVE_QUALITY_FEEDBACK)
                .clearDisplayLiveFeedback()
                .build()

        val secugenSimMatcher =
            fingerprintProto.secugenSimMatcher
                .toBuilder()
                .setVero2(newVero2Configuration)
                .build()
        fingerprintProto.setSecugenSimMatcher(secugenSimMatcher)

        return currentData.toBuilder().setFingerprint(fingerprintProto).build()
    }
}
