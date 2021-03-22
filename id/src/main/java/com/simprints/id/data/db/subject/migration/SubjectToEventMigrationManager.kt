package com.simprints.id.data.db.subject.migration

interface SubjectToEventMigrationManager {

    suspend fun migrateSubjectToSyncToEventsDb()

}
