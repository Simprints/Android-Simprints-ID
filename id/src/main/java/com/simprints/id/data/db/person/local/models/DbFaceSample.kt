package com.simprints.id.data.db.person.local.models

import androidx.room.PrimaryKey
import com.simprints.core.images.SecuredImageRef
import com.simprints.id.data.db.person.domain.FaceSample
import io.realm.RealmObject
import io.realm.annotations.Required

open class DbFaceSample(
    @PrimaryKey
    @Required
    var id: String = "",

    @Required
    var template: ByteArray = byteArrayOf(),

    var imageRef: String? = null
) : RealmObject()

fun DbFaceSample.fromDbToDomain(): FaceSample =
    FaceSample(
        template = template,
        imageRef = imageRef?.let { SecuredImageRef(it) }
    )

fun FaceSample.fromDomainToDb(): DbFaceSample =
    DbFaceSample(
        id = id,
        template = template,
        imageRef = imageRef?.uri
    )
