package com.simprints.id.domain.moduleapi.face.responses.entities

import android.os.Parcelable
import com.simprints.moduleapi.face.responses.entities.IFaceCaptureResult
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceCaptureResult(val index: Int,
                             val result: FaceSample?) : Parcelable

fun IFaceCaptureResult.fromModuleApiToDomain(): FaceCaptureResult =
    FaceCaptureResult(index, sample?.fromModuleApiToDomain())
