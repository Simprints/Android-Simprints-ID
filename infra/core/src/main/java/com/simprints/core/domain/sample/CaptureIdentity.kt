package com.simprints.core.domain.sample

import android.os.Parcelable
import com.simprints.core.domain.modality.Modality
import kotlinx.parcelize.Parcelize

/**
 * CaptureIdentity is a set of biometric template captures from the same yet unknown subject.
 */
@Parcelize
class CaptureIdentity(
    val modality: Modality,
    val samples: List<CaptureSample>,
) : Parcelable
