package com.simprints.feature.orchestrator.usecases


import android.os.Bundle
import com.google.common.truth.Truth.assertThat
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.face.configuration.FaceConfigurationResult
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.fetchsubject.FetchSubjectResult
import com.simprints.feature.setup.SetupResult
import com.simprints.moduleapi.app.responses.IAppErrorResponse
import com.simprints.moduleapi.app.responses.IAppRefusalFormResponse
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
            ExitFormResult(true) to IAppRefusalFormResponse::class.java,
            FetchSubjectResult(found = false) to IAppErrorResponse::class.java,
            SetupResult(permissionGranted = false) to IAppErrorResponse::class.java,
            FaceConfigurationResult(isSuccess = false) to IAppErrorResponse::class.java,
        ).forEach { (result, responseClass) -> assertThat(useCase(result)).isInstanceOf(responseClass) }
    }

    @Test
    fun `Maps successful step results to null`() {
        listOf(
            FetchSubjectResult(found = true),
            SetupResult(permissionGranted = true),
            FaceConfigurationResult(isSuccess = true),
            FaceCaptureResult(emptyList())
        ).forEach { result -> assertThat(useCase(result)).isNull() }
    }

    @Test
    fun `Maps non-result parcelable to null`() {
        assertThat(useCase(Bundle())).isNull()
    }
}
