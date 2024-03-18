package com.simprints.feature.orchestrator.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.fetchsubject.FetchSubjectResult
import com.simprints.feature.setup.SetupResult
import com.simprints.fingerprint.connect.FingerprintConnectResult
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import com.simprints.infra.orchestration.data.responses.AppRefusalResponse
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class MapRefusalOrErrorResultUseCaseTest {

    private lateinit var useCase: MapRefusalOrErrorResultUseCase

    @Before
    fun setUp() {
        useCase = MapRefusalOrErrorResultUseCase()
    }

    @Test
    fun `Maps terminal step results to appropriate response`() {
        mapOf(
            ExitFormResult(true) to AppRefusalResponse::class.java,
            FetchSubjectResult(found = false) to AppErrorResponse::class.java,
            SetupResult(isSuccess  = false) to AppErrorResponse::class.java,
            FingerprintConnectResult(isSuccess = false) to AppErrorResponse::class.java,
        ).forEach { (result, responseClass) -> assertThat(useCase(result)).isInstanceOf(responseClass) }
    }

    @Test
    fun `Maps successful step results to null`() {
        listOf(
            FetchSubjectResult(found = true),
            SetupResult(isSuccess = true),
            FaceCaptureResult(emptyList())
        ).forEach { result -> assertThat(useCase(result)).isNull() }
    }

    @Test
    fun `Maps non-result serializable to null`() {
        assertThat(useCase(mockk())).isNull()
    }
}
