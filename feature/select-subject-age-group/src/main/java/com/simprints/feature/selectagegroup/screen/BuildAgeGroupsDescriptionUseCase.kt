package com.simprints.feature.selectagegroup.screen

import android.content.Context
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.config.store.models.allowedAgeRanges
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.simprints.infra.resources.R.string as IDR

internal class BuildAgeGroupsDescriptionUseCase @Inject constructor(
    private val configurationRepo: ConfigRepository,
    @ApplicationContext private val context: Context,
) {

    /**
     * Builds a list of age groups for display
     * it reads the allowed age ranges from the configuration and formats them for display
     * it also adds a 0-<first age range> and <last age range>-above if they are not present
     * overlapping age ranges are not yet supported
     */
    suspend operator fun invoke(): List<AgeGroupDisplayModel> {
        val allowedAgeRanges = configurationRepo.getProjectConfiguration().allowedAgeRanges()
        return formatAgeRangesForDisplay(allowedAgeRanges)
    }

    private fun formatAgeRangesForDisplay(ageGroups: List<AgeGroup>): List<AgeGroupDisplayModel> {

        // Sorting the age ranges
        val sortedRanges = processAgeGroups(ageGroups).toMutableList()

        return sortedRanges.map { ageGroup ->
            AgeGroupDisplayModel(ageGroup.getDisplayName(), ageGroup)
        }

    }

    private fun processAgeGroups(ageGroups: List<AgeGroup>): List<AgeGroup> {
        // Handle empty list case by returning a single age group starting at 0 and ending with null
        if (ageGroups.isEmpty()) return listOf(AgeGroup(0, null))

        // Flatten all start and end ages into a single list, removing nulls, duplicates, and sorting
        val sortedUniqueAges =
            ageGroups.flatMap { listOf(it.startInclusive, it.endExclusive) }.filterNotNull()
                .sorted().distinct()

        val processedAgeGroups = mutableListOf<AgeGroup>()

        // Ensure the first age group starts at 0
        if (sortedUniqueAges.first() != 0) {
            processedAgeGroups.add(AgeGroup(0, sortedUniqueAges.first()))
        }

        var startAge = sortedUniqueAges.first()

        // Create age groups based on sorted unique ages
        for (i in 1 until sortedUniqueAges.size) {
            val endAge = sortedUniqueAges[i]
            processedAgeGroups.add(AgeGroup(startAge, endAge))
            startAge = endAge
        }

        // Ensure the final age group ends with null
        processedAgeGroups.add(AgeGroup(sortedUniqueAges.last(), null))

        return processedAgeGroups

    }


    private fun getString(id: Int): String {
        return context.getString(id)
    }

    // Helper function to convert months to readable format
    private fun formatAgeInMonthsForDisplay(ageInMonths: Int): String {
        return when {
            ageInMonths < 12 -> "$ageInMonths ${getString(IDR.age_group_selection_months)}"
            ageInMonths < 24 -> "1 ${getString(IDR.age_group_selection_year)}"
            else -> "${ageInMonths / 12} ${getString(IDR.age_group_selection_years)}"
        }
    }

    private fun AgeGroup.getDisplayName(): String {
        val start = formatAgeInMonthsForDisplay(startInclusive)
        val end = endExclusive?.let {
            "${getString(IDR.age_group_selection_age_range_to)} ${
                formatAgeInMonthsForDisplay(it)
            }"
        } ?: getString(IDR.age_group_selection_age_range_and_above)
        return "$start $end"
    }
}

