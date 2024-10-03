package com.simprints.infra.license.models


sealed class Vendor(val value: String) {

    object RankOne : Vendor("RANK_ONE_FACE") {
    }

    object Nec : Vendor("NEC_FINGERPRINT") {
    }

    companion object {
        fun fromKey(key: String): Vendor = when (key) {
            RankOne.value -> RankOne
            Nec.value -> Nec
            else -> throw IllegalStateException("Invalid licence vendor requested")
        }
    }
}
