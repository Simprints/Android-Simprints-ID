package com.simprints.fingerprint.data.domain.person

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*
import com.simprints.fingerprintmatcher.Person as PersonMatcher
import com.simprints.id.domain.Person as PersonCore

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

    companion object {
        fun fromCoreToDomain(corePerson: PersonCore) =
            with(corePerson) {
                Person(patientId, projectId, userId, moduleId, fingerprints.map { Fingerprint.fromCoreToDomain(it) }, createdAt, updatedAt, toSync)
            }
        }
}

fun Person.fromDomainToCore() =
    PersonCore(patientId, projectId, userId, moduleId, fingerprints.map { it.fromDomainToCore() }, createdAt, updatedAt, toSync)

fun Person.fromDomainToMatcher() =
    PersonMatcher(patientId, fingerprints.map { it.fromDomainToMatcher() })
