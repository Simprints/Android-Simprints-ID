package com.simprints.face.configuration

import com.simprints.face.configuration.screen.FaceConfigurationFragmentArgs

object FaceConfigurationContract {

    val DESTINATION_ID = R.id.faceConfigurationFragment

    const val RESULT = "face_configuration_result"

    fun getArgs(
        projectId: String,
        deviceId: String,
    ) = FaceConfigurationFragmentArgs(projectId, deviceId).toBundle()
}
