package com.simprints.infra.enrolment.records.repository.local

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.enrolment.records.repository.local.migration.RealmToRoomMigrationFlagsStore
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SelectEnrolmentRecordLocalDataSourceUseCaseTest {
    @MockK
    private lateinit var roomDataSource: RoomEnrolmentRecordLocalDataSource

    @MockK
    private lateinit var realmDataSource: RealmEnrolmentRecordLocalDataSource

    @MockK
    private lateinit var realmToRoomMigrationFlagsStore: RealmToRoomMigrationFlagsStore

    @InjectMockKs
    private lateinit var useCase: SelectEnrolmentRecordLocalDataSourceUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `invoke should return roomDataSource when migration is completed`() = runTest {
        // Given
        coEvery { realmToRoomMigrationFlagsStore.isMigrationCompleted() } returns true

        // When
        val dataSource = useCase.invoke()

        // Then
        assertThat(dataSource).isEqualTo(roomDataSource)
    }

    @Test
    fun `invoke should return realmDataSource when migration is not completed`() = runTest {
        // Given
        coEvery { realmToRoomMigrationFlagsStore.isMigrationCompleted() } returns false

        // When
        val dataSource = useCase.invoke()

        // Then
        assertThat(dataSource).isEqualTo(realmDataSource)
    }
}
