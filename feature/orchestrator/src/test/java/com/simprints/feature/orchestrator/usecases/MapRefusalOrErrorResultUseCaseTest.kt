package com.simprints.feature.orchestrator.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.fetchsubject.FetchSubjectResult
import com.simprints.feature.selectagegroup.SelectSubjectAgeGroupResult
import com.simprints.feature.setup.SetupResult
import com.simprints.feature.validatepool.ValidateSubjectPoolResult
import com.simprints.fingerprint.connect.FingerprintConnectResult
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.events.SessionEventRepository
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import com.simprints.infra.orchestration.data.responses.AppIdentifyResponse
import com.simprints.infra.orchestration.data.responses.AppRefusalResponse
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class MapRefusalOrErrorResultUseCaseTest {

    @MockK
    private lateinit var eventRepository: SessionEventRepository

    private lateinit var useCase: MapRefusalOrErrorResultUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { eventRepository.getCurrentSessionScope().id } returns "sessionId"

        useCase = MapRefusalOrErrorResultUseCase(eventRepository)
    }

    @Test
    fun `Maps terminal step results to appropriate response`() = runTest {
        mapOf(
            ExitFormResult(true) to AppRefusalResponse::class.java,
            FetchSubjectResult(found = false) to AppErrorResponse::class.java,
            SetupResult(isSuccess = false) to AppErrorResponse::class.java,
            FingerprintConnectResult(isSuccess = false) to AppErrorResponse::class.java,
            AlertResult(buttonKey = "buttonKey") to AppErrorResponse::class.java,
        ).forEach { (result, responseClass) ->
            assertThat(useCase(result, mockk())).isInstanceOf(responseClass)
        }
    }

    @Test
    fun `Maps id pool validation results`() = runTest {
        assertThat(useCase(ValidateSubjectPoolResult(isValid = true), mockk())).isNull()
        assertThat(useCase(ValidateSubjectPoolResult(isValid = false), mockk())).isInstanceOf(AppIdentifyResponse::class.java)
    }

    @Test
    fun `Maps successful step results to null`() = runTest {
        listOf(
            FetchSubjectResult(found = true),
            SetupResult(isSuccess = true),
            FaceCaptureResult(emptyList())
        ).forEach { result -> assertThat(useCase(result, mockk())).isNull() }
    }

    @Test
    fun `Maps non-result serializable to null`() = runTest {
        assertThat(useCase(mockk(), mockk())).isNull()
    }

    @Test
    fun `Maps SelectSubjectAgeGroupResult to appropriate response`() = runTest {
        val projectConfiguration = mockk<ProjectConfiguration>(relaxed = true)

        val ageGroupSupported = AgeGroup(0, 10)
        val ageGroupNotSupported = AgeGroup(11, 20)

        every { projectConfiguration.face?.rankOne?.allowedAgeRange } returns ageGroupSupported

        assertThat(useCase(SelectSubjectAgeGroupResult(ageGroupSupported), projectConfiguration)).isNull()
        assertThat(useCase(SelectSubjectAgeGroupResult(ageGroupNotSupported), projectConfiguration)).isInstanceOf(AppErrorResponse::class.java)
    }
}
