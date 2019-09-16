package com.simprints.id.data.db.person.domain

import android.os.Parcelable
import com.simprints.core.images.SecuredImageRef
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
open class FaceSample(
    open val template: ByteArray,
    open val imageRef: SecuredImageRef?) : Parcelable {

    @IgnoredOnParcel
    open val id: String = UUID.nameUUIDFromBytes(template).toString()
}
