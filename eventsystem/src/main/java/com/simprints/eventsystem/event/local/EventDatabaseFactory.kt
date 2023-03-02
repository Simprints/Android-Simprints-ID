package com.simprints.eventsystem.event.local

internal interface EventDatabaseFactory {
    fun build(): EventRoomDatabase
    fun deleteDatabase()
    fun recreateDatabaseKey()
}

