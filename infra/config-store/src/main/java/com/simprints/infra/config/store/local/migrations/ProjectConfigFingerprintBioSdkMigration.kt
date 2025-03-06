package com.simprints.infra.config.store.local.migrations

import androidx.datastore.core.DataMigration
import com.simprints.infra.config.store.local.models.ProtoFingerprintConfiguration
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber
import javax.inject.Inject

/**
 * Can be removed once all the devices have been updated to 2023.4.0
 */
class ProjectConfigFingerprintBioSdkMigration @Inject constructor() : DataMigration<ProtoProjectConfiguration> {
    override suspend fun cleanUp() {
        Simber.i("Migration of project configuration fingerprint bio sdk is done", tag = MIGRATION)
    }

    override suspend fun shouldMigrate(currentData: ProtoProjectConfiguration) = with(currentData) {
        hasFingerprint() && !(fingerprint.hasNec() || fingerprint.hasSecugenSimMatcher())
    }

    override suspend fun migrate(currentData: ProtoProjectConfiguration): ProtoProjectConfiguration {
        Simber.i("Start migration of project configuration fingerprint bio sdk to Datastore", tag = MIGRATION)

        val fingerprintProto = currentData.fingerprint.toBuilder()

        // 1- rename allowedVeroGenerations to allowedScanners
        fingerprintProto.addAllAllowedScanners(fingerprintProto.allowedVeroGenerationsList)

        // 2- add allowedSdks with value SECUGEN_SIM_MATCHER
        fingerprintProto.addAllAllowedSdks(listOf(ProtoFingerprintConfiguration.ProtoBioSdk.SECUGEN_SIM_MATCHER))

        // 3- add secugenSimMatcher with value of the old fingerprint configuration
        fingerprintProto.setSecugenSimMatcher(
            ProtoFingerprintConfiguration.ProtoFingerprintSdkConfiguration
                .newBuilder()
                .addAllFingersToCapture(fingerprintProto.fingersToCaptureList)
                .setDecisionPolicy(fingerprintProto.decisionPolicy)
                .setComparisonStrategyForVerification(fingerprintProto.comparisonStrategyForVerification)
                .also {
                    if (fingerprintProto.hasVero1()) it.vero1 = fingerprintProto.vero1
                    if (fingerprintProto.hasVero2()) it.vero2 = fingerprintProto.vero2
                }.build(),
        )

        // 4- remove allowedVeroGenerations and old fingerprint configuration
        fingerprintProto.clearAllowedVeroGenerations()
        fingerprintProto.clearFingersToCapture()
        fingerprintProto.clearDecisionPolicy()
        fingerprintProto.clearComparisonStrategyForVerification()
        fingerprintProto.clearVero1()
        fingerprintProto.clearVero2()

        return currentData.toBuilder().setFingerprint(fingerprintProto).build()
    }
}
