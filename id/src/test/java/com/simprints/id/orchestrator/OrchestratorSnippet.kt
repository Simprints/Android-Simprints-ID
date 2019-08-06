package com.simprints.id.orchestrator

import com.simprints.moduleapi.app.responses.IAppEnrolResponse
import com.simprints.moduleapi.app.responses.IAppResponseType
import com.simprints.moduleapi.face.responses.IFaceEnrolResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
internal class AppEnrolResponse(override val guid: String,
                                override val type: IAppResponseType = IAppResponseType.ENROL): IAppEnrolResponse

@Parcelize
internal class FaceEnrolResponse(override val guid: String): IFaceEnrolResponse
