package com.simprints.face.data.moduleapi.face.responses

import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceConfigurationResponse(
    override val type: FaceResponseType = FaceResponseType.CONFIGURATION
) : FaceResponse
