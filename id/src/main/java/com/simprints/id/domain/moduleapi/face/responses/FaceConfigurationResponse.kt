package com.simprints.id.domain.moduleapi.face.responses

import com.simprints.moduleapi.face.responses.IFaceConfigurationResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceConfigurationResponse(
    override val type: FaceResponseType = FaceResponseType.CONFIGURATION
) : FaceResponse

fun IFaceConfigurationResponse.fromModuleApiToDomain() = FaceConfigurationResponse()
