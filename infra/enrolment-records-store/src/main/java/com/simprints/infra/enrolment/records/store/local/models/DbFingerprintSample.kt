package com.simprints.infra.enrolment.records.store.local.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.fingerprint.IFingerIdentifier

@Entity(
    tableName = "fingerprint_samples",
    foreignKeys = [
        ForeignKey(
            entity = DbSubject::class,
            parentColumns = ["subjectId"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE, // ✅ Ensures consistency
        ),
    ],
    indices = [
        Index(value = ["format"]),
        Index(value = ["subjectId"]),

    ],
)
data class DbFingerprintSample(
    @PrimaryKey
    val id: String,
    val subjectId: String, // Foreign Key reference
    val fingerIdentifier: Int,
    val template: ByteArray,
    val templateQualityScore: Int,
    val format: String,
)

internal fun DbFingerprintSample.fromDbToDomain(): FingerprintSample = FingerprintSample(
    id = id,
    fingerIdentifier = IFingerIdentifier.entries[fingerIdentifier],
    template = template,
    templateQualityScore = templateQualityScore,
    format = format,
    subjectId = subjectId,
)

internal fun FingerprintSample.fromDomainToDb(): DbFingerprintSample = DbFingerprintSample(
    id = id,
    fingerIdentifier = fingerIdentifier.ordinal,
    template = template,
    templateQualityScore = templateQualityScore,
    format = format,
    subjectId = subjectId,
)
