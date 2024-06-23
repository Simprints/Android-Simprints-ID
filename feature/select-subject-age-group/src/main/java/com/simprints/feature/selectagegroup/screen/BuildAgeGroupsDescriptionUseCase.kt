package com.simprints.feature.selectagegroup.screen

import android.content.Context
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.config.store.models.allowedAgeRanges
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

internal class BuildAgeGroupsDescriptionUseCase @Inject constructor(
    private val configurationRepo: ConfigRepository,
    @ApplicationContext private val context: Context,
) {

    /**
     * Builds a list of age groups for display
     * it reads the allowed age ranges from the configuration and formats them for display
     * it also adds a 0-<first age range> and <last age range>-above if they are not present
     */
    suspend operator fun invoke(): List<AgeGroupDisplayModel> {
        val allowedAgeRanges = configurationRepo.getProjectConfiguration().allowedAgeRanges()
        val processedAgeRanges = generateSortedUniqueAgeGroups(allowedAgeRanges)
        return processedAgeRanges.map { ageGroup ->
            AgeGroupDisplayModel(ageGroup.getDisplayName(), ageGroup)
        }
    }

    private fun generateSortedUniqueAgeGroups(ageGroups: List<AgeGroup>): List<AgeGroup> {
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

    // Helper function to convert months to readable format
    private fun formatAgeInMonthsForDisplay(ageInMonths: Int): String {
        val years = ageInMonths / 12
        val remainingMonths = ageInMonths % 12

        val yearsString = context.resources.getQuantityString(
            IDR.plurals.age_group_selection_age_in_years, years, years
        )
        val monthsString = context.resources.getQuantityString(
            IDR.plurals.age_group_selection_age_in_months, ageInMonths, ageInMonths
        )

        val remainingMonthsString = context.resources.getQuantityString(
            IDR.plurals.age_group_selection_age_in_months, remainingMonths, remainingMonths
        )
        return when {
            years == 0 -> monthsString
            remainingMonths == 0 -> yearsString
            else -> "$yearsString, $remainingMonthsString"
        }
    }

    private fun AgeGroup.getDisplayName(): String {
        val start = formatAgeInMonthsForDisplay(startInclusive)
        val end = endExclusive?.let {
            "${context.getString(IDR.string.age_group_selection_age_range_to)} ${
                formatAgeInMonthsForDisplay(it)
            }"
        } ?: context.getString(IDR.string.age_group_selection_age_range_and_above)
        return "$start $end"
    }
}
