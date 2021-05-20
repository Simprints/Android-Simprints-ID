package com.simprints.id.data.db.subject.local.models

import androidx.annotation.Keep
import androidx.room.PrimaryKey
import com.simprints.core.domain.face.FaceSample
import com.simprints.eventsystem.event.domain.models.face.FaceTemplateFormat
import com.simprints.moduleapi.face.responses.entities.IFaceTemplateFormat
import io.realm.RealmObject
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

fun DbFaceSample.fromDbToDomain(): FaceSample =
    FaceSample(template = template, format = IFaceTemplateFormat.valueOf(format))

fun FaceSample.fromDomainToDb(): DbFaceSample =
    DbFaceSample(
        id = id,
        template = template,
        format = format.name
    )
