package com.simprints.infra.enrolment.records.repository.local

import com.simprints.infra.enrolment.records.repository.local.migration.RealmToRoomMigrationFlagsStore
import javax.inject.Inject

internal class SelectEnrolmentRecordLocalDataSourceUseCase @Inject constructor(
    private val roomDataSource: RoomEnrolmentRecordLocalDataSource,
    private val realmDataSource: RealmEnrolmentRecordLocalDataSource,
    private val realmToRoomMigrationFlagsStore: RealmToRoomMigrationFlagsStore,
) {
    suspend operator fun invoke() = if (realmToRoomMigrationFlagsStore.isMigrationCompleted()) {
        roomDataSource
    } else {
        realmDataSource
    }
}
