package com.simprints.id.data.db.person.domain

import android.os.Parcelable
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
open class FaceSample(
    open val template: ByteArray) : Parcelable {

    @IgnoredOnParcel
    open val id: String by lazy {
        UUID.nameUUIDFromBytes(template).toString()
    }
}

fun FaceCaptureSample.fromModuleEntityToDomain() = FaceSample(template)

