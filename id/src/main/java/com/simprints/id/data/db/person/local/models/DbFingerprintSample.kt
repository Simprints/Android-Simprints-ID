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

    var fingerIdentifier: Int = -1,

    @Required
    var template: ByteArray = byteArrayOf(),

    var templateQualityScore: Int = -1,

    var imageRef: String? = null
) : RealmObject()

fun DbFingerprintSample.fromDbToDomain(): FingerprintSample =
    FingerprintSample(
        fingerIdentifier = FingerIdentifier.values()[fingerIdentifier],
        template = template,
        imageRef = imageRef?.let { SecuredImageRef(it) },
        templateQualityScore = templateQualityScore
    )

fun FingerprintSample.fromDomainToDb(): DbFingerprintSample =
    DbFingerprintSample(
        id,
        fingerIdentifier.ordinal,
        template,
        templateQualityScore,
        imageRef?.uri
    )
