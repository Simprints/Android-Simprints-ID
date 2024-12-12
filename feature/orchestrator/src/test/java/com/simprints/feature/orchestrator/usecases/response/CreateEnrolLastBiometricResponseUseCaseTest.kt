package com.simprints.feature.orchestrator.usecases.response

import com.google.common.truth.Truth
import com.simprints.feature.enrollast.EnrolLastBiometricResult
import com.simprints.infra.orchestration.data.responses.AppEnrolResponse
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class CreateEnrolLastBiometricResponseUseCaseTest {
    private lateinit var useCase: CreateEnrolLastBiometricResponseUseCase

    @Before
    fun setUp() {
        useCase = CreateEnrolLastBiometricResponseUseCase()
    }

    @Test
    fun `Converts correct results to response`() {
        Truth
            .assertThat(
                useCase(
                    listOf(
                        EnrolLastBiometricResult("1234"),
                        mockk(),
                    ),
                ),
            ).isInstanceOf(AppEnrolResponse::class.java)
    }

    @Test
    fun `Returns error if no valid response`() {
        Truth.assertThat(useCase(emptyList())).isInstanceOf(AppErrorResponse::class.java)
    }
}
