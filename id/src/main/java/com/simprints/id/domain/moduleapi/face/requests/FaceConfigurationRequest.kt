package com.simprints.id.domain.moduleapi.face.requests

import com.simprints.moduleapi.face.requests.IFaceConfigurationRequest
import kotlinx.parcelize.Parcelize

@Parcelize
data class FaceConfigurationRequest(
    val projectId: String,
    val deviceId: String,
    override val type: FaceRequestType = FaceRequestType.CONFIGURATION
) : FaceRequest

fun FaceConfigurationRequest.fromDomainToModuleApi(): IFaceConfigurationRequest =
    IFaceConfigurationRequestImpl(projectId, deviceId)

@Parcelize
private class IFaceConfigurationRequestImpl(override val projectId: String,
                                            override val deviceId: String) : IFaceConfigurationRequest
