package com.simprints.infra.enrolment.records.repository.local.migration

import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordAction
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
        val enrolmentRecordActions = listOf<EnrolmentRecordAction>(mockk<EnrolmentRecordAction.Creation>(relaxed = true))
        val project = mockk<Project>()

        coEvery { realmToRoomMigrationFlagsStore.isMigrationInProgress() } returns true
        coJustRun { roomEnrolmentRecordLocalDataSource.performActions(any(), any()) }

        insertRecordsInRoomDuringMigrationUseCase.invoke(enrolmentRecordActions, project)

        coVerify { roomEnrolmentRecordLocalDataSource.performActions(actions = enrolmentRecordActions, project = project) }
    }

    @Test
    fun `invoke should not call performActions when migration is not in progress`() = runBlocking {
        val enrolmentRecordActions = listOf<EnrolmentRecordAction>(mockk<EnrolmentRecordAction.Creation>())
        val project = mockk<Project>()

        coEvery { realmToRoomMigrationFlagsStore.isMigrationInProgress() } returns false

        insertRecordsInRoomDuringMigrationUseCase.invoke(enrolmentRecordActions, project)

        coVerify(exactly = 0) { roomEnrolmentRecordLocalDataSource.performActions(any(), any()) }
    }
}
