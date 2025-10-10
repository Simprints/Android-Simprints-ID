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
    val multifactorId: MultiFactorIdConfiguration?,
    val custom: Map<String, Any>?,
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

fun ProjectConfiguration.isSimprintsEventDownSyncAllowed(): Boolean = synchronization.down.simprints != null &&
    synchronization.down.simprints.frequency != Frequency.ONLY_PERIODICALLY_UP_SYNC

fun ProjectConfiguration.isCommCareEventDownSyncAllowed(): Boolean = synchronization.down.commCare != null

fun ProjectConfiguration.imagesUploadRequiresUnmeteredConnection(): Boolean = synchronization.up.simprints.imagesRequireUnmeteredConnection

fun ProjectConfiguration.isSampleUploadEnabledInProject(): Boolean = listOfNotNull(
    face?.rankOne?.imageSavingStrategy?.let { it != FaceConfiguration.ImageSavingStrategy.NEVER },
    face?.simFace?.imageSavingStrategy?.let { it != FaceConfiguration.ImageSavingStrategy.NEVER },
    fingerprint
        ?.nec
        ?.vero2
        ?.imageSavingStrategy
        ?.let { it != Vero2Configuration.ImageSavingStrategy.NEVER },
    fingerprint
        ?.secugenSimMatcher
        ?.vero2
        ?.imageSavingStrategy
        ?.let { it != Vero2Configuration.ImageSavingStrategy.NEVER },
).let { explicitStrategies ->
    explicitStrategies.isNotEmpty() && explicitStrategies.any { it }
}

fun ProjectConfiguration.allowedAgeRanges(): List<AgeGroup> = listOfNotNull(
    face?.rankOne?.allowedAgeRange,
    face?.simFace?.allowedAgeRange,
    fingerprint?.secugenSimMatcher?.allowedAgeRange,
    fingerprint?.nec?.allowedAgeRange,
)

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
        sortedUniqueAges.last(),
        null,
    )
}

fun ProjectConfiguration.isAgeRestricted() = allowedAgeRanges().any { !it.isEmpty() }

fun ProjectConfiguration.experimental(): ExperimentalProjectConfiguration = ExperimentalProjectConfiguration(custom)

// module sync

fun ProjectConfiguration.isProjectWithModuleSync(): Boolean =
    synchronization.down.simprints?.partitionType == DownSynchronizationConfiguration.PartitionType.MODULE

fun ProjectConfiguration.isProjectWithPeriodicallyUpSync(): Boolean =
    synchronization.up.simprints.frequency == Frequency.ONLY_PERIODICALLY_UP_SYNC

fun ProjectConfiguration.isModuleSelectionAvailable(): Boolean = isProjectWithModuleSync() && !isProjectWithPeriodicallyUpSync()

fun ProjectConfiguration.determineFaceSDKs(ageGroup: AgeGroup?): List<FaceConfiguration.BioSdk> {
    if (!isAgeRestricted()) {
        return face?.allowedSDKs.orEmpty()
    }

    return buildList {
        ageGroup?.let { age ->
            if (face?.rankOne?.allowedAgeRange?.contains(age) == true) {
                add(FaceConfiguration.BioSdk.RANK_ONE)
            }
            if (face?.simFace?.allowedAgeRange?.contains(age) == true) {
                add(FaceConfiguration.BioSdk.SIM_FACE)
            }
        }
    }
}

fun ProjectConfiguration.determineFingerprintSDKs(ageGroup: AgeGroup?): List<FingerprintConfiguration.BioSdk> {
    if (!isAgeRestricted()) {
        return fingerprint?.allowedSDKs.orEmpty()
    }

    return buildList {
        ageGroup?.let { age ->
            if (fingerprint?.secugenSimMatcher?.allowedAgeRange?.contains(age) == true) {
                add(FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER)
            }
            if (fingerprint?.nec?.allowedAgeRange?.contains(age) == true) {
                add(FingerprintConfiguration.BioSdk.NEC)
            }
        }
    }
}
