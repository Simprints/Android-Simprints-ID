package com.simprints.face.configuration

import com.simprints.face.configuration.screen.FaceConfigurationFragmentArgs

object FaceConfigurationContract {

    val DESTINATION = R.id.faceConfigurationFragment

    fun getArgs(
        projectId: String,
        deviceId: String,
    ) = FaceConfigurationFragmentArgs(projectId, deviceId).toBundle()
}
