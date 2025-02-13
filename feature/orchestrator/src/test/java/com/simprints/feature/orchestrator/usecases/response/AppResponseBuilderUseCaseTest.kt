package com.simprints.feature.orchestrator.usecases.response

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class AppResponseBuilderUseCaseTest {
    @MockK
    lateinit var isNewEnrolment: IsNewEnrolmentUseCase

    @MockK
    lateinit var handleEnrolment: CreateEnrolResponseUseCase

    @MockK
    lateinit var handleIdentify: CreateIdentifyResponseUseCase

    @MockK
    lateinit var handleVerify: CreateVerifyResponseUseCase

    @MockK
    lateinit var handleConfirmIdentity: CreateConfirmIdentityResponseUseCase

    @MockK
    lateinit var handleEnrolLastBiometric: CreateEnrolLastBiometricResponseUseCase

    private lateinit var useCase: AppResponseBuilderUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        coEvery { handleEnrolment.invoke(any(), any(), any()) } returns mockk()
        coEvery { handleIdentify.invoke(any(), any()) } returns mockk()
        every { handleVerify.invoke(any(), any()) } returns mockk()
        every { handleConfirmIdentity.invoke(any()) } returns mockk()
        every { handleEnrolLastBiometric.invoke(any()) } returns mockk()

        useCase = AppResponseBuilderUseCase(
            isNewEnrolment,
            handleEnrolment,
            handleIdentify,
            handleVerify,
            handleConfirmIdentity,
            handleEnrolLastBiometric,
        )
    }

    @Test
    fun `Handles as enrolment for new enrolment action`() = runTest {
        every { isNewEnrolment(any(), any()) } returns true
        useCase(mockk(), mockk<ActionRequest.EnrolActionRequest>(), mockk(), mockk())
        coVerify { handleEnrolment.invoke(any(), any(), any()) }
    }

    @Test
    fun `Handles as identification for enrolment action with existing item`() = runTest {
        every { isNewEnrolment(any(), any()) } returns false
        useCase(mockk(), mockk<ActionRequest.EnrolActionRequest>(), mockk(), mockk())
        coVerify { handleIdentify.invoke(any(), any()) }
    }

    @Test
    fun `Handles as identification for identification action`() = runTest {
        useCase(mockk(), mockk<ActionRequest.IdentifyActionRequest>(), mockk(), mockk())
        coVerify { handleIdentify.invoke(any(), any()) }
    }

    @Test
    fun `Handles as verification for verification action`() = runTest {
        useCase(mockk(), mockk<ActionRequest.VerifyActionRequest>(), mockk(), mockk())
        coVerify { handleVerify.invoke(any(), any()) }
    }

    @Test
    fun `Handles as confirmIdentity for confirm action`() = runTest {
        useCase(mockk(), mockk<ActionRequest.ConfirmIdentityActionRequest>(), mockk(), mockk())
        coVerify { handleConfirmIdentity.invoke(any()) }
    }

    @Test
    fun `Handles as enrol last biometric for enrol last action`() = runTest {
        useCase(mockk(), mockk<ActionRequest.EnrolLastBiometricActionRequest>(), mockk(), mockk())
        coVerify { handleEnrolLastBiometric.invoke(any()) }
    }

    @Test
    fun `Handles null request`() = runTest {
        assertThat(useCase(mockk(), null, mockk(), mockk())).isInstanceOf(AppErrorResponse::class.java)
    }
}
