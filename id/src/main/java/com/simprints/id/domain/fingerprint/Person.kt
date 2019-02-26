package com.simprints.id.domain.fingerprint

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Person(
    val guid: String,
    var fingerprints: List<Fingerprint> = emptyList()): Parcelable {

    init {
        fingerprints = fingerprints
            .groupBy { it.fingerId }
            .mapValues { it.value.maxBy { it.qualityScore } }
            .values
            .toMutableList() as MutableList<Fingerprint>
    }
}
