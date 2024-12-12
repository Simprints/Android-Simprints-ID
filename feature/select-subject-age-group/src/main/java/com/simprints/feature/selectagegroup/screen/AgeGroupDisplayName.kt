package com.simprints.feature.selectagegroup.screen

import android.content.Context
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.resources.R as IDR

/**
 * Formats age range for display.
 */
fun AgeGroup.displayName(context: Context): String {
    val start = formatAgeInMonthsForDisplay(context, startInclusive)
    val end = endExclusive?.let {
        val toLabel = context.getString(IDR.string.age_group_selection_age_range_to)
        val ageLabel = formatAgeInMonthsForDisplay(context, it)
        "$toLabel $ageLabel"
    } ?: context.getString(IDR.string.age_group_selection_age_range_and_above)
    return "$start $end"
}

// Helper function to convert months to readable format
private fun formatAgeInMonthsForDisplay(
    context: Context,
    ageInMonths: Int,
): String {
    val years = ageInMonths / 12
    val remainingMonths = ageInMonths % 12

    val yearsString = context.resources.getQuantityString(
        IDR.plurals.age_group_selection_age_in_years,
        years,
        years,
    )
    val monthsString = context.resources.getQuantityString(
        IDR.plurals.age_group_selection_age_in_months,
        ageInMonths,
        ageInMonths,
    )

    val remainingMonthsString = context.resources.getQuantityString(
        IDR.plurals.age_group_selection_age_in_months,
        remainingMonths,
        remainingMonths,
    )
    return when {
        years == 0 -> monthsString
        remainingMonths == 0 -> yearsString
        else -> "$yearsString, $remainingMonthsString"
    }
}
