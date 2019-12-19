package com.simprints.id.data.db.person.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

// FaceRecord = FaceSample + patient Id (it will be used for matchings)
@Parcelize
class FaceRecord(val personId: String,
                 override val template: ByteArray) : FaceSample(template), Parcelable

