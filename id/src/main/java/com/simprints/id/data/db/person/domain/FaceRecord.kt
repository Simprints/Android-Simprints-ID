package com.simprints.id.data.db.person.domain

import android.os.Parcelable
import com.simprints.core.images.SecuredImageRef
import kotlinx.android.parcel.Parcelize

@Parcelize
class FaceRecord(val personId: String,
                 override val template: ByteArray,
                 override val imageRef: SecuredImageRef?) : FaceSample(template, imageRef), Parcelable

