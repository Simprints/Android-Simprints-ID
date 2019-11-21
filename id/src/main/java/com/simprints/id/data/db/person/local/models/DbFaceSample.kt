package com.simprints.id.data.db.person.local.models

import androidx.room.PrimaryKey
import com.simprints.id.data.db.person.domain.FaceSample
import io.realm.RealmObject
import io.realm.annotations.Required

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
