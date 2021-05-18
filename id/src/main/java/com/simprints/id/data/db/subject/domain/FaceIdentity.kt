package com.simprints.eventsystem.subject.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class FaceIdentity(val personId: String, val faces: List<FaceSample>) : Parcelable

