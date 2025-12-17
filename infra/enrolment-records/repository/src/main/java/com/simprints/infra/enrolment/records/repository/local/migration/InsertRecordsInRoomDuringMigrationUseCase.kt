package com.simprints.infra.enrolment.records.repository.local.migration

import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordAction
import com.simprints.infra.enrolment.records.repository.local.RoomEnrolmentRecordLocalDataSource
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.REALM_DB_MIGRATION
import com.simprints.infra.logging.Simber
import javax.inject.Inject

// Should be completely removed when all users are migrated to Room
internal class InsertRecordsInRoomDuringMigrationUseCase @Inject constructor(
    private val realmToRoomMigrationFlagsStore: RealmToRoomMigrationFlagsStore,
    private val roomEnrolmentRecordLocalDataSource: RoomEnrolmentRecordLocalDataSource,
) {
    suspend operator fun invoke(
        actions: List<EnrolmentRecordAction>,
        project: Project,
    ) {
        // if the realm to room migration is in progress, we need to insert the records in the new db too

        if (realmToRoomMigrationFlagsStore.isMigrationInProgress()) {
            actions.filterIsInstance<EnrolmentRecordAction.Creation>().forEach {
                insertRecordInRoom(it, project)
            }
            logUpdatesDuringMigration(actions)
        }
    }

    private suspend fun insertRecordInRoom(
        creation: EnrolmentRecordAction.Creation,
        project: Project,
    ) {
        roomEnrolmentRecordLocalDataSource.performActions(actions = listOf(creation), project)
        Simber.i(
            "[InsertRecordsInRoomDuringMigrationUseCase] Inserted subject ${creation.enrolmentRecord.subjectId} enrolled during migration",
            tag = REALM_DB_MIGRATION,
        )
    }

    private fun logUpdatesDuringMigration(actions: List<EnrolmentRecordAction>) {
        if (actions.any { it is EnrolmentRecordAction.Update || it is EnrolmentRecordAction.Deletion }) {
            // if actions contains any updates or deletes and the migration is in progress, log them as an error
            Simber.e(
                "[EnrolmentRecordRepositoryImpl] Actions during migration: ${actions.joinToString(", ")}",
                IllegalStateException(
                    "Actions during migration are not allowed. Please ensure that the migration is complete before performing updates or deletions.",
                ),
                tag = REALM_DB_MIGRATION,
            )
        }
    }
}
