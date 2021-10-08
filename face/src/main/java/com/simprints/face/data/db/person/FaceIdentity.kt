package com.simprints.face.data.db.person

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FaceIdentity(val faceId: String, val faces: List<FaceSample>) : Parcelable
