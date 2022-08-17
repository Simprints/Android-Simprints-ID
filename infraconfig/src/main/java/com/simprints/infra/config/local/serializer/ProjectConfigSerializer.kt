package com.simprints.infra.config.local.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.simprints.infra.config.domain.models.*
import com.simprints.infra.config.local.models.ProtoProjectConfiguration
import com.simprints.infra.config.local.models.toProto
import java.io.InputStream
import java.io.OutputStream

@Suppress("BlockingMethodInNonBlockingContext")
internal object ProjectConfigSerializer : Serializer<ProtoProjectConfiguration> {
    override val defaultValue: ProtoProjectConfiguration =
        ProjectConfiguration(
            projectId = "",
            general = GeneralConfiguration(
                modalities = listOf(GeneralConfiguration.Modality.FINGERPRINT),
                languageOptions = listOf(),
                defaultLanguage = "",
                collectLocation = true,
                duplicateBiometricEnrolmentCheck = false,
            ),
            face = null,
            fingerprint = FingerprintConfiguration(
                fingersToCapture = listOf(
                    FingerprintConfiguration.Finger.LEFT_THUMB,
                    FingerprintConfiguration.Finger.LEFT_INDEX_FINGER
                ),
                qualityThreshold = 60,
                decisionPolicy = DecisionPolicy(0, 0, 700),
                allowedVeroGenerations = listOf(FingerprintConfiguration.VeroGeneration.VERO_1),
                comparisonStrategyForVerification = FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
                displayHandIcons = true,
                vero2 = null,
            ),
            consent = ConsentConfiguration(
                programName = "this program",
                organizationName = "This organization",
                collectConsent = true,
                displaySimprintsLogo = true,
                allowParentalConsent = false,
                generalPrompt = ConsentConfiguration.ConsentPromptConfiguration(
                    enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
                    dataSharedWithPartner = false,
                    dataUsedForRAndD = false,
                    privacyRights = true,
                    confirmation = true,
                ),
                parentalPrompt = null,
            ),
            identification = IdentificationConfiguration(
                maxNbOfReturnedCandidates = 10,
                poolType = IdentificationConfiguration.PoolType.USER,
            ),
            synchronization = SynchronizationConfiguration(
                frequency = SynchronizationConfiguration.Frequency.PERIODICALLY,
                up = UpSynchronizationConfiguration(
                    simprints = UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration(
                        kind = UpSynchronizationConfiguration.UpSynchronizationKind.NONE
                    ),
                    coSync = UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration(
                        kind = UpSynchronizationConfiguration.UpSynchronizationKind.NONE
                    )
                ),
                down = DownSynchronizationConfiguration(
                    partitionType = DownSynchronizationConfiguration.PartitionType.USER,
                    maxNbOfModules = 6,
                    moduleOptions = listOf(),
                ),
            ),
        ).toProto()

    override suspend fun readFrom(input: InputStream): ProtoProjectConfiguration {
        try {
            return ProtoProjectConfiguration.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: ProtoProjectConfiguration, output: OutputStream) =
        t.writeTo(output)
}
