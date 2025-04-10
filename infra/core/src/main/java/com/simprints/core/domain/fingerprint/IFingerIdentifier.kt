package com.simprints.core.domain.fingerprint

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports

@Keep
@ExcludedFromGeneratedTestCoverageReports("Enum")
// **IMPORTANT**: Do NOT change the order of this enum as it is used in the database by index.
// Changing the order of the entries in this enum will lead to data corruption or mismatches
// when retrieving or storing data in the database. Always append new entries at the end.
enum class IFingerIdentifier(
    val id: Int,
) {
    RIGHT_5TH_FINGER(0),
    RIGHT_4TH_FINGER(1),
    RIGHT_3RD_FINGER(2),
    RIGHT_INDEX_FINGER(3),
    RIGHT_THUMB(4),
    LEFT_THUMB(5),
    LEFT_INDEX_FINGER(6),
    LEFT_3RD_FINGER(7),
    LEFT_4TH_FINGER(8),
    LEFT_5TH_FINGER(9),
    ;

    // create from id
    companion object {
        fun fromId(id: Int) = entries.firstOrNull { it.id == id } ?: throw IllegalArgumentException("Invalid id: $id")
    }
}
