package com.simprints.infra.enrolment.records.repository.local.migration

import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.repository.local.RoomEnrolmentRecordLocalDataSource
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class InsertRecordsInRoomDuringMigrationUseCaseTest {
    @MockK
    private lateinit var realmToRoomMigrationFlagsStore: RealmToRoomMigrationFlagsStore

    @MockK
    private lateinit var roomEnrolmentRecordLocalDataSource: RoomEnrolmentRecordLocalDataSource

    private lateinit var insertRecordsInRoomDuringMigrationUseCase: InsertRecordsInRoomDuringMigrationUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        insertRecordsInRoomDuringMigrationUseCase = InsertRecordsInRoomDuringMigrationUseCase(
            realmToRoomMigrationFlagsStore,
            roomEnrolmentRecordLocalDataSource,
        )
    }

    @Test
    fun `invoke should call performActions when migration is in progress`() = runBlocking {
        val subjectAction = listOf<SubjectAction>(mockk<SubjectAction.Creation>(relaxed = true))
        val project = mockk<Project>()

        coEvery { realmToRoomMigrationFlagsStore.isMigrationInProgress() } returns true
        coJustRun { roomEnrolmentRecordLocalDataSource.performActions(any(), any()) }

        insertRecordsInRoomDuringMigrationUseCase.invoke(subjectAction, project)

        coVerify { roomEnrolmentRecordLocalDataSource.performActions(actions = subjectAction, project = project) }
    }

    @Test
    fun `invoke should not call performActions when migration is not in progress`() = runBlocking {
        val subjectAction = listOf<SubjectAction>(mockk<SubjectAction.Creation>())
        val project = mockk<Project>()

        coEvery { realmToRoomMigrationFlagsStore.isMigrationInProgress() } returns false

        insertRecordsInRoomDuringMigrationUseCase.invoke(subjectAction, project)

        coVerify(exactly = 0) { roomEnrolmentRecordLocalDataSource.performActions(any(), any()) }
    }
}
