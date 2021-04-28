package com.simprints.face.configuration

sealed class ConfigurationState {
    object Started : ConfigurationState()
    object Downloading : ConfigurationState()
    data class FinishedWithSuccess(val license: String) : ConfigurationState()
    data class FinishedWithError(val errorCode: String) : ConfigurationState()
}
