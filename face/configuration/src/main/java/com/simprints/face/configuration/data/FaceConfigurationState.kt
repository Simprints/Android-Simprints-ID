package com.simprints.face.configuration.data

internal sealed class FaceConfigurationState {
    data object Started : FaceConfigurationState()
    data object Downloading : FaceConfigurationState()
    data class FinishedWithSuccess(val license: String) : FaceConfigurationState()
    data class FinishedWithError(val errorCode: String) : FaceConfigurationState()
    data class FinishedWithBackendMaintenanceError(val estimatedOutage: Long?) : FaceConfigurationState()
}
