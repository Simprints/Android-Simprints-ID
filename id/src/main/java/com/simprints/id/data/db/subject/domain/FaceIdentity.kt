package com.simprints.id.data.db.person.domain

import android.os.Parcelable
import com.simprints.id.data.db.subject.domain.FaceSample
import kotlinx.android.parcel.Parcelize

@Parcelize
class FaceIdentity(val personId: String, val faces: List<FaceSample>) : Parcelable

