package com.simprints.id.domain.fingerprint

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

//@Parcelize
//data class Person(
//    val guid: String,
//    var fingerprints: List<Fingerprint> = emptyList()): Parcelable {
//
//    init {
//        fingerprints = fingerprints
//            .groupBy { it.fingerId }
//            .mapValues { it.value.maxBy { it.qualityScore } }
//            .values
//            .toMutableList() as MutableList<Fingerprint>
//    }
//}

@Parcelize
data class Person (
    val patientId: String,
    val projectId: String,
    val userId: String,
    val moduleId: String,
    var fingerprints: List<Fingerprint>,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    var toSync: Boolean = true
): Parcelable {
    init {
        fingerprints = fingerprints
            .groupBy { it.fingerId }
            .mapValues { it.value.maxBy { it.qualityScore } }
            .values
            .toMutableList() as MutableList<Fingerprint>
    }
}
