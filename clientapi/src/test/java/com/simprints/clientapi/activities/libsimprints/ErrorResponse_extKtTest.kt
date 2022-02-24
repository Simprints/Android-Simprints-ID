package com.simprints.clientapi.activities.libsimprints

import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.libsimprints.Constants
import org.junit.Test

class ErrorResponseExtKtTest {

    @Test
    fun libSimprintsResultCode_backendMaintenanceError_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.BACKEND_MAINTENANCE_ERROR
        val libSimprintsreason = Constants.SIMPRINTS_CANCELLED

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }

    @Test
    fun libSimprintsResultCode_unexpectedError_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.UNEXPECTED_ERROR
        val libSimprintsreason = Constants.SIMPRINTS_CANCELLED

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }

    @Test
    fun libSimprintsResultCode_guidNotFoundError_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.GUID_NOT_FOUND_ONLINE
        val libSimprintsreason = Constants.SIMPRINTS_VERIFY_GUID_NOT_FOUND_ONLINE

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }
}
