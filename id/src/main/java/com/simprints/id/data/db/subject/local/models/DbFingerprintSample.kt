package com.simprints.id.data.db.subject.local.models

import androidx.annotation.Keep
import androidx.room.PrimaryKey
import com.simprints.id.data.db.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.data.db.subject.domain.FingerprintSample
import io.realm.RealmObject
import io.realm.annotations.Required

@Keep
open class DbFingerprintSample(
    @PrimaryKey
    @Required
    var id: String = "",

    var fingerIdentifier: Int = -1,

    @Required
    var template: ByteArray = byteArrayOf(),

    var templateQualityScore: Int = -1,

    /**
     * Realm doesn't accept enums, we need to save the formats as Strings. This is a [FingerprintTemplateFormat].
     */
    @Required
    var format: String = ""

) : RealmObject()

fun DbFingerprintSample.fromDbToDomain(): FingerprintSample =
    FingerprintSample(
        fingerIdentifier = FingerIdentifier.values()[fingerIdentifier],
        template = template,
        templateQualityScore = templateQualityScore,
        format = FingerprintTemplateFormat.valueOf(format)
    )

fun FingerprintSample.fromDomainToDb(): DbFingerprintSample =
    DbFingerprintSample(
        id,
        fingerIdentifier.ordinal,
        template,
        templateQualityScore,
        format.name
    )
