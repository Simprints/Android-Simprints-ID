package com.simprints.feature.selectagegroup.screen

import android.content.Context
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.allowedAgeRanges
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.simprints.infra.resources.R.string as IDR

internal class BuildAgeGroupsUseCase @Inject constructor(
    private val configurationRepo: ConfigRepository,
    @ApplicationContext private val context: Context,
) {

    suspend operator fun invoke(): List<AgeGroup> {
        val allowedAgeRanges = configurationRepo.getProjectConfiguration().allowedAgeRanges()
        return formatAgeRangesForDisplay(allowedAgeRanges)
    }

    private fun formatAgeRangesForDisplay(allowedAgeRanges: List<IntRange>): List<AgeGroup> {
        val result = mutableListOf<AgeGroup>()

        // Sorting the age ranges
        val sortedRanges = allowedAgeRanges.sortedBy { it.first }
        // Starting point
        var startAge = 0

        for (range in sortedRanges) {
            if (range.first > startAge) {
                result.add(
                    AgeGroup(
                        "${formatAgeInMonthsForDisplay(startAge)} ${getString(IDR.age_group_selection_age_range_to)} ${formatAgeInMonthsForDisplay(range.first)
                        }", range
                    )
                )
            }
            if (range.last != Int.MAX_VALUE) {
                startAge = range.last + 1
            } else {
                startAge = range.first
                break
            }
        }

        result.add(
            AgeGroup(
                "${formatAgeInMonthsForDisplay(startAge)} ${getString(IDR.age_group_selection_age_range_and_above)}",
                startAge..Int.MAX_VALUE
            )
        )

        return result
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
}
