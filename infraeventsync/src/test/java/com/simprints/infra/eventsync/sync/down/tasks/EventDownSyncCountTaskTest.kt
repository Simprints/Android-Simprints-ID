package com.simprints.infra.eventsync.sync.down.tasks

import com.simprints.infra.eventsync.SampleSyncScopes.projectDownSyncScope
import com.simprints.infra.eventsync.event.remote.EventRemoteDataSource
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class EventDownSyncCountTaskTest {

    @MockK
    lateinit var eventDownSyncScopeRepository: EventDownSyncScopeRepository

    @MockK
    lateinit var eventRemoteDataSource: EventRemoteDataSource

    lateinit var eventDownSyncCountTask: EventDownSyncCountTask

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        eventDownSyncCountTask = EventDownSyncCountTask(eventDownSyncScopeRepository, eventRemoteDataSource)
    }

    @Test
    fun `calls refresh state for each operation`() = runTest {
        eventDownSyncCountTask.getCount(projectDownSyncScope)

        coVerify { eventDownSyncScopeRepository.refreshState(any()) }
    }

    @Test
    fun `calls remote data source for count`() = runTest {
        eventDownSyncCountTask.getCount(projectDownSyncScope)

        coVerify { eventRemoteDataSource.count(any()) }
    }
}
