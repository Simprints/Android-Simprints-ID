package com.simprints.infra.enrolment.records.repository.domain.models

import android.os.Parcelable
import com.simprints.core.domain.ear.EarSample
import kotlinx.parcelize.Parcelize

@Parcelize
class EarIdentity(
    val subjectId: String,
    val ears: List<EarSample>,
) : Parcelable
