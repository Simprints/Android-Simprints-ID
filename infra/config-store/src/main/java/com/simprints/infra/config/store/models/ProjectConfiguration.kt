package com.simprints.infra.config.store.models

data class ProjectConfiguration(
    val projectId: String,
    val updatedAt: String,
    val general: GeneralConfiguration,
    val face: FaceConfiguration?,
    val fingerprint: FingerprintConfiguration?,
    val consent: ConsentConfiguration,
    val identification: IdentificationConfiguration,
    val synchronization: SynchronizationConfiguration,
)

fun ProjectConfiguration.canCoSyncAllData(): Boolean =
    synchronization.up.coSync.kind == UpSynchronizationConfiguration.UpSynchronizationKind.ALL

fun ProjectConfiguration.canCoSyncBiometricData(): Boolean =
    synchronization.up.coSync.kind == UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS

fun ProjectConfiguration.canCoSyncAnalyticsData(): Boolean =
    synchronization.up.coSync.kind == UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS

fun ProjectConfiguration.canCoSyncData(): Boolean =
    synchronization.up.coSync.kind != UpSynchronizationConfiguration.UpSynchronizationKind.NONE

fun ProjectConfiguration.canSyncDataToSimprints(): Boolean =
    synchronization.up.simprints.kind != UpSynchronizationConfiguration.UpSynchronizationKind.NONE

fun ProjectConfiguration.canSyncAllDataToSimprints(): Boolean =
    synchronization.up.simprints.kind == UpSynchronizationConfiguration.UpSynchronizationKind.ALL

fun ProjectConfiguration.canSyncBiometricDataToSimprints(): Boolean =
    synchronization.up.simprints.kind == UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS

fun ProjectConfiguration.canSyncAnalyticsDataToSimprints(): Boolean =
    synchronization.up.simprints.kind == UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS

fun ProjectConfiguration.isEventDownSyncAllowed(): Boolean =
    synchronization.frequency != SynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC

fun ProjectConfiguration.imagesUploadRequiresUnmeteredConnection(): Boolean =
    synchronization.up.simprints.imagesRequireUnmeteredConnection

fun ProjectConfiguration.allowedAgeRanges(): List<AgeGroup> {
    return listOf(
        face?.rankOne?.allowedAgeRange,
        fingerprint?.secugenSimMatcher?.allowedAgeRange,
        fingerprint?.nec?.allowedAgeRange
    ).filterNotNull().filterNot { it.isEmpty() }
}

fun ProjectConfiguration.isAgeRestricted() = allowedAgeRanges().isNotEmpty()
