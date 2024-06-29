package com.simprints.infra.facebiosdk.matching

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FaceIdentity(val subjectId: String, val faces: List<FaceSample>) : Parcelable
