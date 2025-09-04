package com.simprints.infra.enrolment.records.repository.domain.models

import android.os.Parcelable
import com.simprints.core.domain.sample.Sample
import kotlinx.parcelize.Parcelize

@Parcelize
class FaceIdentity(
    val subjectId: String,
    val faces: List<Sample>,
) : Parcelable
