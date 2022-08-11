package com.simprints.infra.realm.models

import androidx.annotation.Keep
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

@Keep
open class DbFaceSample(
    @PrimaryKey
    @Required
    var id: String = "",

    @Required
    var template: ByteArray = byteArrayOf(),

    /**
     * Realm doesn't accept enums, we need to save the formats as Strings. This is a [FaceTemplateFormat].
     */
    @Required
    var format: String = ""
) : RealmObject()

