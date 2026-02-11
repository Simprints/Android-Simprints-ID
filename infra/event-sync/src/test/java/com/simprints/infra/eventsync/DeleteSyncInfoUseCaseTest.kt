package com.simprints.infra.eventsync

import com.simprints.infra.eventsync.event.commcare.cache.CommCareSyncCache
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.up.EventUpSyncScopeRepository
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class DeleteSyncInfoUseCaseTest {
    @MockK
    private lateinit var commCareSyncCache: CommCareSyncCache

    @MockK
    private lateinit var downSyncScopeRepository: EventDownSyncScopeRepository

    @MockK
    private lateinit var eventSyncCache: EventSyncCache

    @MockK
    private lateinit var upSyncScopeRepository: EventUpSyncScopeRepository

    private lateinit var useCase: DeleteSyncInfoUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = DeleteSyncInfoUseCase(
            commCareSyncCache = commCareSyncCache,
            downSyncScopeRepository = downSyncScopeRepository,
            eventSyncCache = eventSyncCache,
            upSyncScopeRepo = upSyncScopeRepository,
        )
    }

    @Test
    fun `deletes all sync state and clears caches`() = runTest {
        useCase()

        coVerify(exactly = 1) { downSyncScopeRepository.deleteAll() }
        coVerify(exactly = 1) { commCareSyncCache.clearAllSyncedCases() }
        coVerify(exactly = 1) { upSyncScopeRepository.deleteAll() }
        coVerify(exactly = 1) { eventSyncCache.clearProgresses() }
        coVerify(exactly = 1) { eventSyncCache.storeLastSuccessfulSyncTime(null) }
    }
}
