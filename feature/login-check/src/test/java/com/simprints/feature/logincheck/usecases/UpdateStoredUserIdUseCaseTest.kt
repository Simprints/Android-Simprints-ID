package com.simprints.feature.logincheck.usecases

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.authstore.AuthStore
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class UpdateStoredUserIdUseCaseTest {
    @MockK
    private lateinit var authStore: AuthStore

    private lateinit var useCase: UpdateStoredUserIdUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = UpdateStoredUserIdUseCase(authStore)
    }

    @Test
    fun `Does not update userId when already present in store`() {
        every { authStore.signedInUserId } returns "userId".asTokenizableRaw()

        useCase("userId".asTokenizableRaw())

        verify(exactly = 0) { authStore.setProperty("signedInUserId").value(any<TokenizableString.Raw>()) }
    }

    @Test
    fun `Does updates userId when not present in store`() {
        every { authStore.signedInUserId } returns null

        useCase("userId".asTokenizableRaw())

        verify { authStore.setProperty("signedInUserId").value(any<TokenizableString.Raw>()) }
    }
}
