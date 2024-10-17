package com.simprints.infra.config.store.models

data class ProjectConfiguration(
    val id: String,
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
    return listOfNotNull(
        face?.rankOne?.allowedAgeRange,
        fingerprint?.secugenSimMatcher?.allowedAgeRange,
        fingerprint?.nec?.allowedAgeRange
    )
}

/**
 * Returns a list of all age groups a subject can fall into based on the allowed age ranges
 * of the project's face and fingerprint SDK configurations.
 * Note that some of these age groups might be unsupported!
 */
fun ProjectConfiguration.sortedUniqueAgeGroups(): List<AgeGroup> {
    val ageGroups = allowedAgeRanges()

    // Handle empty list case by returning a single age group starting at 0 and ending with null
    if (ageGroups.isEmpty()) return listOf(AgeGroup(0, null))

    // Flatten all start and end ages into a single list, removing nulls, duplicates, and sorting.
    var sortedUniqueAges =
        ageGroups.flatMap { listOf(it.startInclusive, it.endExclusive) }.filterNotNull()
    // Ensure the first age group starts at 0
    sortedUniqueAges = (listOf(0) + sortedUniqueAges).sorted().distinct()
    // Create age groups based on sorted unique ages
    return sortedUniqueAges.zipWithNext { start, end -> AgeGroup(start, end) } + AgeGroup(
        sortedUniqueAges.last(), null
    )
}

fun ProjectConfiguration.isAgeRestricted() = allowedAgeRanges().any { !it.isEmpty()}
