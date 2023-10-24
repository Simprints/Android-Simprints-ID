package com.simprints.id.domain.moduleapi.face.responses

import kotlinx.parcelize.Parcelize

@Parcelize
data class FaceConfigurationResponse(
    override val type: FaceResponseType = FaceResponseType.CONFIGURATION
) : FaceResponse
