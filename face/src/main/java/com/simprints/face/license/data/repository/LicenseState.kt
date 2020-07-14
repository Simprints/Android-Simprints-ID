package com.simprints.face.license.data.repository

sealed class LicenseState {
    object Started : LicenseState()
    object Downloading : LicenseState()
    data class FinishedWithSuccess(val license: String) : LicenseState()
    object FinishedWithError : LicenseState()
}
