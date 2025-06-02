package com.simprints.infra.enrolment.records.repository.local.migration

import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.repository.local.RoomEnrolmentRecordLocalDataSource
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.REALM_DB_MIGRATION
import com.simprints.infra.logging.Simber
import javax.inject.Inject

// Should be completely removed when all users are migrated to Room
internal class InsertRecordsDuringMigrationUseCase @Inject constructor(
    private val realmToRoomMigrationFlagsStore: RealmToRoomMigrationFlagsStore,
    private val roomEnrolmentRecordLocalDataSource: RoomEnrolmentRecordLocalDataSource,
) {
    suspend operator fun invoke(
        subjectAction: SubjectAction.Creation,
        project: Project,
    ) {
        // if the realm to room migration is in progress, we need to insert the records in the new db too
        if (realmToRoomMigrationFlagsStore.isMigrationInProgress()) {
            roomEnrolmentRecordLocalDataSource.performActions(actions = listOf(subjectAction), project)
            Simber.i(
                "[InsertRecordsDuringMigrationUseCase] Inserted subject ${subjectAction.subject.subjectId} enrolled during migration",
                tag = REALM_DB_MIGRATION,
            )
        }
    }
}
