package com.simprints.face.data.moduleapi.face.responses

import com.simprints.face.controllers.core.events.model.RefusalAnswer
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
class FaceExitFormResponse(val reason: RefusalAnswer, val extra: String) : FaceResponse {
    @IgnoredOnParcel
    override val type: FaceResponseType = FaceResponseType.EXIT_FORM
}
