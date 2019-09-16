package com.simprints.id.data.db.person.local.models

import androidx.room.PrimaryKey
import com.simprints.core.images.SecuredImageRef
import com.simprints.id.data.db.person.domain.FingerIdentifier
import com.simprints.id.data.db.person.domain.FingerprintSample
import io.realm.RealmObject
import io.realm.annotations.Required

open class DbFingerprintSample(
    @PrimaryKey
    @Required
    var id: String = "",
    var fingerIdentifier: String = "",
    var template: ByteArray = byteArrayOf(),
    var imageRef: String? = null,
    var qualityScore: Int = -1
) : RealmObject()

fun DbFingerprintSample.fromDbToDomain(): FingerprintSample =
    FingerprintSample(
        fingerIdentifier = FingerIdentifier.valueOf(fingerIdentifier),
        template = template,
        imageRef = imageRef?.let { SecuredImageRef(it) },
        templateQualityScore = qualityScore
    )

fun FingerprintSample.fromDomainToDb(): DbFingerprintSample =
    DbFingerprintSample(
        id,
        fingerIdentifier.name,
        template,
        imageRef?.uri,
        templateQualityScore
    )
