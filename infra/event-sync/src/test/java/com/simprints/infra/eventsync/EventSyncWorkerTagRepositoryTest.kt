package com.simprints.infra.eventsync

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

internal class EventSyncWorkerTagRepositoryTest {
    @MockK
    private lateinit var timeHelper: TimeHelper

    private lateinit var repository: EventSyncWorkerTagRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = EventSyncWorkerTagRepository(timeHelper)
    }

    @Test
    fun `getPeriodicWorkTags should return hardcoded periodic tags and scheduled time tag`() {
        every { timeHelper.now() } returns Timestamp(ms = 1234L)

        val result = repository.getPeriodicWorkTags()

        assertThat(result)
            .containsExactly(
                "TAG_MASTER_SYNC_SCHEDULERS",
                "TAG_MASTER_SYNC_SCHEDULER_PERIODIC_TIME",
                "TAG_SCHEDULED_AT_1234",
            ).inOrder()
    }

    @Test
    fun `getOneTimeWorkTags should return hardcoded one time tags and scheduled time tag`() {
        every { timeHelper.now() } returns Timestamp(ms = 5678L)

        val result = repository.getOneTimeWorkTags()

        assertThat(result)
            .containsExactly(
                "TAG_MASTER_SYNC_SCHEDULERS",
                "TAG_MASTER_SYNC_SCHEDULER_ONE_TIME",
                "TAG_SCHEDULED_AT_5678",
            ).inOrder()
    }

    @Test
    fun `getAllWorkerTag should return hardcoded all workers tag`() {
        val result = repository.getAllWorkerTag()

        assertThat(result).isEqualTo("TAG_SUBJECTS_SYNC_ALL_WORKERS")
    }
}
