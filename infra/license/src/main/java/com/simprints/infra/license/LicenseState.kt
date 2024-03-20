package com.simprints.infra.license

sealed class LicenseState {
    data object Started : LicenseState()
    data object Downloading : LicenseState()
    data class FinishedWithSuccess(val license: String) : LicenseState()
    data class FinishedWithError(val errorCode: String) : LicenseState()
    data class FinishedWithBackendMaintenanceError(val estimatedOutage: Long?) : LicenseState() {
    }
}
