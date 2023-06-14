package com.simprints.infra.realm.models

import androidx.annotation.Keep
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
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
