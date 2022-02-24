package com.simprints.id.domain.moduleapi.app

import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.moduleapi.app.DomainToModuleApiAppResponse.fromDomainModuleApiAppResponse
import com.simprints.id.domain.moduleapi.app.DomainToModuleApiAppResponse.fromDomainToModuleApiAppErrorResponse
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.moduleapi.app.responses.IAppErrorReason
import com.simprints.moduleapi.app.responses.IAppResponse
import org.junit.Test

class DomainToModuleApiAppResponseTest {

    @Test
    fun fromDomainToModuleApiAppErrorReason_backendMaintenanceError_shoulMapCorrectly() {
        val appErrorResponse = AppErrorResponse(AppErrorResponse.Reason.BACKEND_MAINTENANCE_ERROR)

       val response = fromDomainToModuleApiAppErrorResponse(appErrorResponse)

        assertThat(response.reason).isInstanceOf(IAppErrorReason.BACKEND_MAINTENANCE_ERROR::class.java)
    }

    @Test
    fun fromDomainToModuleApiAppErrorReason_differentProjectSignedInError_shoulMapCorrectly() {
        val appErrorResponse = AppErrorResponse(AppErrorResponse.Reason.DIFFERENT_PROJECT_ID_SIGNED_IN)

        val response = fromDomainToModuleApiAppErrorResponse(appErrorResponse)

        assertThat(response.reason).isInstanceOf(IAppErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN::class.java)
    }
}
