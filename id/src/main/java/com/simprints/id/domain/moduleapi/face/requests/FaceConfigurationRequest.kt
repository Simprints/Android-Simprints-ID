package com.simprints.id.domain.moduleapi.face.requests

import com.simprints.moduleapi.face.requests.IFaceConfigurationRequest
import kotlinx.parcelize.Parcelize

@Parcelize
data class FaceConfigurationRequest(
    override val type: FaceRequestType = FaceRequestType.CONFIGURATION,
    val projectId: String,
    val deviceId: String
) : FaceRequest

fun FaceConfigurationRequest.fromDomainToModuleApi(): IFaceConfigurationRequest =
    IFaceConfigurationRequestImpl(projectId, deviceId)

@Parcelize
private class IFaceConfigurationRequestImpl(override val projectId: String,
                                            override val deviceId: String) : IFaceConfigurationRequest
