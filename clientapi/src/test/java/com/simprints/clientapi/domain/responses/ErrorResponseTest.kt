package com.simprints.clientapi.domain.responses

import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.domain.responses.ErrorResponse.Reason.Companion.fromModuleApiToDomain
import com.simprints.moduleapi.app.responses.IAppErrorReason
import org.junit.Test

class ErrorResponseTest {

    @Test
    fun iAppReason_backendMaintenanceError_fromModuleApiToDomain() {
        val apiReason = IAppErrorReason.BACKEND_MAINTENANCE_ERROR
        val domainReason = ErrorResponse.Reason.BACKEND_MAINTENANCE_ERROR
        assertThat(fromModuleApiToDomain(apiReason)).isEqualTo(domainReason)
    }

    @Test
    fun iAppReason_faceConfigurationError_fromModuleApiToDomain() {
        val apiReason = IAppErrorReason.FACE_CONFIGURATION_ERROR
        val domainReason = ErrorResponse.Reason.FACE_CONFIGURATION_ERROR
        assertThat(fromModuleApiToDomain(apiReason)).isEqualTo(domainReason)
    }

    @Test
    fun iAppReason_faceLicenseInvalidError_fromModuleApiToDomain() {
        val apiReason = IAppErrorReason.FACE_LICENSE_INVALID
        val domainReason = ErrorResponse.Reason.FACE_LICENSE_INVALID
        assertThat(fromModuleApiToDomain(apiReason)).isEqualTo(domainReason)
    }
}
