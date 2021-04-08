package com.simprints.id.data.license.repository

sealed class LicenseState {
    object Started : LicenseState()
    object Downloading : LicenseState()
    data class FinishedWithSuccess(val license: String) : LicenseState()
    data class FinishedWithError(val errorCode: String) : LicenseState()
}
