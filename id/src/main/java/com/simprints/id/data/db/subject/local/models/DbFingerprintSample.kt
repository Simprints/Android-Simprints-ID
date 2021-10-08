package com.simprints.id.data.db.subject.local.models

import androidx.annotation.Keep
import androidx.room.PrimaryKey
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.IFingerprintTemplateFormat
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
        fingerIdentifier = IFingerIdentifier.values()[fingerIdentifier],
        template = template,
        templateQualityScore = templateQualityScore,
        format = IFingerprintTemplateFormat.valueOf(format)
    )

fun FingerprintSample.fromDomainToDb(): DbFingerprintSample =
    DbFingerprintSample(
        id,
        fingerIdentifier.ordinal,
        template,
        templateQualityScore,
        format.name
    )
