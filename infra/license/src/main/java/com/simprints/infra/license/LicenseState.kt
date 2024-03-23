package com.simprints.infra.license

import com.simprints.infra.license.remote.License

sealed class LicenseState {
    data object Started : LicenseState()
    data object Downloading : LicenseState()
    data class FinishedWithSuccess(val license: License) : LicenseState()
    data class FinishedWithError(val errorCode: String) : LicenseState()
    data class FinishedWithBackendMaintenanceError(val estimatedOutage: Long?) : LicenseState() {
    }
}
