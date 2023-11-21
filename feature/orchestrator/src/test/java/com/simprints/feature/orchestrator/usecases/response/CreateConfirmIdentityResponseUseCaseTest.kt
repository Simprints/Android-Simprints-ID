package com.simprints.feature.orchestrator.usecases.response

import android.os.Bundle
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.orchestration.data.responses.AppConfirmationResponse
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import com.simprints.feature.selectsubject.SelectSubjectResult
import org.junit.Before
import org.junit.Test

class CreateConfirmIdentityResponseUseCaseTest {

    private lateinit var useCase: CreateConfirmIdentityResponseUseCase

    @Before
    fun setUp() {
        useCase = CreateConfirmIdentityResponseUseCase()
    }

    @Test
    fun `Converts correct results to response`() {
        assertThat(useCase(listOf(
            SelectSubjectResult(true),
            Bundle(),
        ))).isInstanceOf(AppConfirmationResponse::class.java)
    }

    @Test
    fun `Returns error if no valid response`() {
        assertThat(useCase(emptyList())).isInstanceOf(AppErrorResponse::class.java)
    }
}
