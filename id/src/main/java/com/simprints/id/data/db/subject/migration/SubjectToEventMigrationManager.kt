package com.simprints.eventsystem.subject.migration

interface SubjectToEventMigrationManager {

    suspend fun migrateSubjectToSyncToEventsDb()

}
