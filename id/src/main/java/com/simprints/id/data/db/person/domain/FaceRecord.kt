package com.simprints.id.data.db.person.domain

import android.os.Parcelable
import com.simprints.core.images.SecuredImageRef
import kotlinx.android.parcel.Parcelize

// FaceRecord = FaceSample + patient Id (it will be used for matchings)
@Parcelize
class FaceRecord(val personId: String,
                 override val template: ByteArray,
                 override val imageRef: SecuredImageRef?) : FaceSample(template, imageRef), Parcelable

