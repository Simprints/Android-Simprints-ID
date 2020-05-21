package com.simprints.face.data.db.person

import android.os.Parcelable
import com.simprints.face.data.moduleapi.face.responses.entities.FaceSample
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceIdentity(val faceId: String, val faces: List<FaceSample>) : Parcelable
