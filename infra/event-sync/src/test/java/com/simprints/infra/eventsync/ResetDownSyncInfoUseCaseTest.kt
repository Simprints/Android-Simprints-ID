package com.simprints.infra.eventsync

import com.simprints.infra.eventsync.event.commcare.cache.CommCareSyncCache
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class ResetDownSyncInfoUseCaseTest {
    @MockK
    private lateinit var commCareSyncCache: CommCareSyncCache

    @MockK
    private lateinit var downSyncScopeRepository: EventDownSyncScopeRepository

    private lateinit var useCase: ResetDownSyncInfoUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = ResetDownSyncInfoUseCase(
            commCareSyncCache = commCareSyncCache,
            downSyncScopeRepository = downSyncScopeRepository,
        )
    }

    @Test
    fun `deletes down sync state and clears CommCare cache`() = runTest {
        useCase()

        coVerify(exactly = 1) { downSyncScopeRepository.deleteAll() }
        coVerify(exactly = 1) { commCareSyncCache.clearAllSyncedCases() }
    }
}
