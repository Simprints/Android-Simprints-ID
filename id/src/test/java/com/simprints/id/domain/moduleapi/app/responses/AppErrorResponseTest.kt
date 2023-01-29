package com.simprints.id.domain.moduleapi.app.responses

import com.google.common.truth.Truth
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse.Reason.Companion.fromDomainAlertTypeToAppErrorType
import org.junit.Test


internal class AppErrorResponseTest {


    @Test
    fun `test INTEGRITY_SERVICE_ERROR is mapped to UNEXPECTED_ERROR`() {
        val reason =fromDomainAlertTypeToAppErrorType(AlertType.INTEGRITY_SERVICE_ERROR)
        Truth.assertThat(reason).isEqualTo(AppErrorResponse.Reason.UNEXPECTED_ERROR)
    }
}
