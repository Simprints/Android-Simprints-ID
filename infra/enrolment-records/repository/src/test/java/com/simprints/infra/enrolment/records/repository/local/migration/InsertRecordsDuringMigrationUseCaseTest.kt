package com.simprints.infra.enrolment.records.repository.local.migration

import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.repository.local.RoomEnrolmentRecordLocalDataSource
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class InsertRecordsDuringMigrationUseCaseTest {
    @MockK
    private lateinit var realmToRoomMigrationFlagsStore: RealmToRoomMigrationFlagsStore

    @MockK
    private lateinit var roomEnrolmentRecordLocalDataSource: RoomEnrolmentRecordLocalDataSource

    private lateinit var insertRecordsDuringMigrationUseCase: InsertRecordsDuringMigrationUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        insertRecordsDuringMigrationUseCase = InsertRecordsDuringMigrationUseCase(
            realmToRoomMigrationFlagsStore,
            roomEnrolmentRecordLocalDataSource,
        )
    }

    @Test
    fun `invoke should call performActions when migration is in progress`() = runBlocking {
        val subjectAction = mockk<SubjectAction.Creation>(relaxed = true)
        val project = mockk<Project>()

        coEvery { realmToRoomMigrationFlagsStore.isMigrationInProgress() } returns true
        coJustRun { roomEnrolmentRecordLocalDataSource.performActions(any(), any()) }

        insertRecordsDuringMigrationUseCase.invoke(subjectAction, project)

        coVerify { roomEnrolmentRecordLocalDataSource.performActions(actions = listOf(subjectAction), project = project) }
    }

    @Test
    fun `invoke should not call performActions when migration is not in progress`() = runBlocking {
        val subjectAction = mockk<SubjectAction.Creation>()
        val project = mockk<Project>()

        coEvery { realmToRoomMigrationFlagsStore.isMigrationInProgress() } returns false

        insertRecordsDuringMigrationUseCase.invoke(subjectAction, project)

        coVerify(exactly = 0) { roomEnrolmentRecordLocalDataSource.performActions(any(), any()) }
    }
}
