package com.simprints.moduleapi.face.responses

import android.os.Parcelable
import com.simprints.moduleapi.face.responses.entities.IFaceMatchingResult


interface IFaceIdentifyResponse : Parcelable, IFaceResponse {

    val identifications: List<IFaceMatchingResult>
}
