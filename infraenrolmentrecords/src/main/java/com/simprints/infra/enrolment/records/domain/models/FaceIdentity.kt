package com.simprints.infra.enrolment.records.domain.models

import android.os.Parcelable
import com.simprints.core.domain.face.FaceSample
import kotlinx.parcelize.Parcelize

@Parcelize
class FaceIdentity(val personId: String, val faces: List<FaceSample>) : Parcelable

