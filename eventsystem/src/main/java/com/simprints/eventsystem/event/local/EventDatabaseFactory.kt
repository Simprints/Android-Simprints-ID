package com.simprints.eventsystem.event.local

interface EventDatabaseFactory {
    fun build(): EventRoomDatabase
    fun deleteDatabase()
    fun recreateDatabaseKey()
}

