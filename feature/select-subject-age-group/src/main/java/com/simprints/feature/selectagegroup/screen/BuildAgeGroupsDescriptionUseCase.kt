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
        val sortedRanges = ageGroups.sortedBy { it.startInclusive }.toMutableList()

        // Add initial item if no age group starts with 0
        if (sortedRanges.isEmpty() || sortedRanges.first().startInclusive != 0) {
            sortedRanges.add(0, AgeGroup(0, sortedRanges.firstOrNull()?.startInclusive))
        }
        // Add final item if no age group ends with null
        if (sortedRanges.none { it.endExclusive == null }) {
            sortedRanges.add(AgeGroup(sortedRanges.last().endExclusive ?: 0, null))
        }
        return sortedRanges.map { ageGroup ->
            AgeGroupDisplayModel(ageGroup.getDisplayName(), ageGroup)
        }

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

