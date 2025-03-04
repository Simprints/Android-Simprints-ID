package com.simprints.infra.config.store.local.migrations

import com.google.common.truth.Truth
import com.simprints.infra.config.store.local.models.ProtoFinger
import com.simprints.infra.config.store.local.models.ProtoFingerprintConfiguration
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import com.simprints.infra.config.store.local.models.toProto
import com.simprints.infra.config.store.models.Vero1Configuration
import com.simprints.infra.config.store.testtools.protoDecisionPolicy
import com.simprints.infra.config.store.testtools.protoVero2Configuration
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ProjectConfigFingerprintBioSdkMigrationTest {
    @Test
    fun `should migrate if fingerprint has no nec and no secugen sim matcher`() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .setFingerprint(
                ProtoFingerprintConfiguration
                    .newBuilder()
                    .build(),
            ).build()

        val shouldMigrate = ProjectConfigFingerprintBioSdkMigration().shouldMigrate(currentData)

        Truth.assertThat(shouldMigrate).isTrue()
    }

    @Test
    fun `should not migrate if doesn't have fingerprint`() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .build()

        val shouldMigrate = ProjectConfigFingerprintBioSdkMigration().shouldMigrate(currentData)

        Truth.assertThat(shouldMigrate).isFalse()
    }

    @Test
    fun `should not migrate if fingerprint has nec`() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .setFingerprint(
                ProtoFingerprintConfiguration
                    .newBuilder()
                    .setNec(
                        ProtoFingerprintConfiguration.ProtoFingerprintSdkConfiguration
                            .newBuilder()
                            .build(),
                    ).build(),
            ).build()

        val shouldMigrate = ProjectConfigFingerprintBioSdkMigration().shouldMigrate(currentData)

        Truth.assertThat(shouldMigrate).isFalse()
    }

    @Test
    fun `should not migrate if fingerprint has secugen sim matcher`() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .setFingerprint(
                ProtoFingerprintConfiguration
                    .newBuilder()
                    .setSecugenSimMatcher(
                        ProtoFingerprintConfiguration.ProtoFingerprintSdkConfiguration
                            .newBuilder()
                            .build(),
                    ).build(),
            ).build()

        val shouldMigrate = ProjectConfigFingerprintBioSdkMigration().shouldMigrate(currentData)

        Truth.assertThat(shouldMigrate).isFalse()
    }

    @Test
    fun `should migrate allowedVeroGenerations to allowedScanners`() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .setFingerprint(
                ProtoFingerprintConfiguration
                    .newBuilder()
                    .addAllAllowedVeroGenerations(
                        listOf(
                            ProtoFingerprintConfiguration.VeroGeneration.VERO_1,
                            ProtoFingerprintConfiguration.VeroGeneration.VERO_2,
                        ),
                    ).build(),
            ).build()

        val migratedData = ProjectConfigFingerprintBioSdkMigration().migrate(currentData)

        Truth.assertThat(migratedData.fingerprint.allowedScannersList).containsExactlyElementsIn(
            listOf(
                ProtoFingerprintConfiguration.VeroGeneration.VERO_1,
                ProtoFingerprintConfiguration.VeroGeneration.VERO_2,
            ),
        )
    }

    @Test
    fun `should create allowedSdks with value SECUGEN_SIM_MATCHER`() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .setFingerprint(
                ProtoFingerprintConfiguration
                    .newBuilder()
                    .build(),
            ).build()

        val migratedData = ProjectConfigFingerprintBioSdkMigration().migrate(currentData)

        Truth
            .assertThat(
                migratedData.fingerprint.allowedSdksList,
            ).containsExactlyElementsIn(
                listOf(ProtoFingerprintConfiguration.ProtoBioSdk.SECUGEN_SIM_MATCHER),
            )
    }

    @Test
    fun `should create secugenSimMatcher with value of the old fingerprint configuration`() = runTest {
        val currentData = ProtoProjectConfiguration
            .newBuilder()
            .setFingerprint(
                ProtoFingerprintConfiguration
                    .newBuilder()
                    .addAllFingersToCapture(
                        listOf(
                            ProtoFinger.LEFT_3RD_FINGER,
                            ProtoFinger.RIGHT_3RD_FINGER,
                        ),
                    ).setDecisionPolicy(protoDecisionPolicy)
                    .setComparisonStrategyForVerification(
                        ProtoFingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
                    ).setVero1(Vero1Configuration(60).toProto())
                    .setVero2(protoVero2Configuration)
                    .build(),
            ).build()

        val migratedData = ProjectConfigFingerprintBioSdkMigration().migrate(currentData)

        Truth
            .assertThat(
                migratedData.fingerprint.secugenSimMatcher.decisionPolicy,
            ).isEqualTo(protoDecisionPolicy)
        Truth
            .assertThat(
                migratedData.fingerprint.secugenSimMatcher.comparisonStrategyForVerification,
            ).isEqualTo(
                ProtoFingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
            )
        Truth
            .assertThat(
                migratedData.fingerprint.secugenSimMatcher.fingersToCaptureList,
            ).containsExactlyElementsIn(
                listOf(
                    ProtoFinger.LEFT_3RD_FINGER,
                    ProtoFinger.RIGHT_3RD_FINGER,
                ),
            )
        Truth
            .assertThat(
                migratedData.fingerprint.secugenSimMatcher.vero1,
            ).isEqualTo(
                Vero1Configuration(60).toProto(),
            )
        Truth
            .assertThat(
                migratedData.fingerprint.secugenSimMatcher.vero2,
            ).isEqualTo(
                protoVero2Configuration,
            )
    }
}
