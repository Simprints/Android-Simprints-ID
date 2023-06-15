package com.simprints.infra.events.event.local

internal interface EventDatabaseFactory {
    fun build(): EventRoomDatabase
    fun deleteDatabase()
    fun recreateDatabaseKey()
}

