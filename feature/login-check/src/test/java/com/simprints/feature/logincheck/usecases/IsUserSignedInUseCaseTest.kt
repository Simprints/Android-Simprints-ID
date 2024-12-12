package com.simprints.feature.logincheck.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.logincheck.usecases.IsUserSignedInUseCase.SignedInState
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.security.SecurityManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

internal class IsUserSignedInUseCaseTest {
    @MockK
    private lateinit var authStore: AuthStore

    @MockK
    private lateinit var secureDataManager: SecurityManager

    private lateinit var useCase: IsUserSignedInUseCase

    private val request = ActionFactory.getFlowRequest()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = IsUserSignedInUseCase(authStore, secureDataManager)
    }

    @Test
    fun `Returns NOT_SIGNED_IN if authStore is empty`() {
        every { authStore.signedInProjectId } returns ""

        assertThat(useCase(request)).isEqualTo(SignedInState.NOT_SIGNED_IN)
    }

    @Test
    fun `Returns MISMATCHED_PROJECT_ID if action project ID does not match auth store`() {
        every { authStore.signedInProjectId } returns "otherProjectId"

        assertThat(useCase(request)).isEqualTo(SignedInState.MISMATCHED_PROJECT_ID)
    }

    @Test
    fun `Returns NOT_SIGNED_IN if local key fetch throws error`() {
        every { authStore.signedInProjectId } returns ActionFactory.MOCK_PROJECT_ID
        every { secureDataManager.getLocalDbKeyOrThrow(any()) } throws Exception()

        assertThat(useCase(request)).isEqualTo(SignedInState.NOT_SIGNED_IN)
    }

    @Test
    fun `Returns NOT_SIGNED_IN if not signed into firebase`() {
        every { authStore.signedInProjectId } returns ActionFactory.MOCK_PROJECT_ID
        every { authStore.isFirebaseSignedIn(any()) } returns false

        assertThat(useCase(request)).isEqualTo(SignedInState.NOT_SIGNED_IN)
    }

    @Test
    fun `Returns SIGNED_IN if signed into firebase`() {
        every { authStore.signedInProjectId } returns ActionFactory.MOCK_PROJECT_ID
        every { authStore.isFirebaseSignedIn(any()) } returns true

        assertThat(useCase(request)).isEqualTo(SignedInState.SIGNED_IN)
    }
}
