package com.simprints.feature.troubleshooting.overview.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.authstore.AuthStore
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CollectIdsUseCaseTest {
    @MockK
    private lateinit var authStore: AuthStore

    private lateinit var useCase: CollectIdsUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { authStore.signedInUserId } returns TokenizableString.Raw(USER_ID)
        every { authStore.signedInProjectId } returns PROJECT_ID

        useCase = CollectIdsUseCase(DEVICE_ID, authStore)
    }

    @Test
    fun `result contains ids from all sources`() = runTest {
        val ids = useCase()

        assertThat(ids).contains(PROJECT_ID)
        assertThat(ids).contains(DEVICE_ID)
        assertThat(ids).contains(USER_ID)
    }

    companion object {
        private const val PROJECT_ID = "projectId"
        private const val DEVICE_ID = "deviceId"
        private const val USER_ID = "userId"
    }
}
