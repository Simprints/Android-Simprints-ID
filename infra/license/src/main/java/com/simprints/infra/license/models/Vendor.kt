package com.simprints.infra.license.models

import com.simprints.infra.license.models.comparators.DefaultVersionComparator
import com.simprints.infra.license.models.comparators.SemanticVersionComparator

sealed class Vendor(
    val value: String,
) {
    abstract val versionComparator: Comparator<String>

    data object RankOne : Vendor("RANK_ONE_FACE") {
        override val versionComparator: Comparator<String>
            get() = SemanticVersionComparator()
    }

    data object Nec : Vendor("NEC_FINGERPRINT") {
        override val versionComparator: Comparator<String>
            get() = DefaultVersionComparator()
    }

    companion object {
        fun fromKey(key: String): Vendor = when (key) {
            RankOne.value -> RankOne
            Nec.value -> Nec
            else -> error("Invalid licence vendor requested")
        }

        fun listAll() = listOf(RankOne, Nec)
    }
}
