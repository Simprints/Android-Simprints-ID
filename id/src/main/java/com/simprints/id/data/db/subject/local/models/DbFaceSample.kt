package com.simprints.id.data.db.subject.local.models

import androidx.annotation.Keep
import androidx.room.PrimaryKey
import com.simprints.id.data.db.subject.domain.FaceSample
import io.realm.RealmObject
import io.realm.annotations.Required

@Keep
open class DbFaceSample(
    @PrimaryKey
    @Required
    var id: String = "",

    @Required
    var template: ByteArray = byteArrayOf()

) : RealmObject()

fun DbFaceSample.fromDbToDomain(): FaceSample =
    FaceSample(template = template)

fun FaceSample.fromDomainToDb(): DbFaceSample =
    DbFaceSample(
        id = id,
        template = template
    )
