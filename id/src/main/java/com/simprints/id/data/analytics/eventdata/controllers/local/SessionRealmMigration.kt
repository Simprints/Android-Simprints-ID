package com.simprints.id.data.analytics.eventdata.controllers.local

import com.simprints.id.data.analytics.eventdata.models.local.*
import io.realm.DynamicRealm
import io.realm.RealmMigration
import io.realm.RealmSchema
import io.realm.annotations.RealmModule

internal class SessionRealmMigration : RealmMigration {

    //StopShip: classes renamed. Migration Required.
    @RealmModule(classes = [DbDatabaseInfo::class, DbDevice::class, DbLocation::class, DbEvent::class, DbSession::class])
    class SessionModule

    companion object {
        const val REALM_SCHEMA_VERSION: Long = 3
        const val DB_INFO_TABLE = "DbDatabaseInfo"
        const val DB_DEVICE_TABLE = "DbDevice"
        const val DB_LOCATION_TABLE = "DbLocation"
        const val DB_EVENT_TABLE = "DbEvent"
        const val DB_SESSION_TABLE = "DbSession"
    }

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        for (i in oldVersion..newVersion) {
            when (i.toInt()) {
                3 -> migrateTo4(realm.schema)
            }
        }
    }

    private fun migrateTo4(schema: RealmSchema) {
        schema.get("RlDatabaseInfo")?.className = DB_INFO_TABLE
        schema.get("RlDevice")?.className = DB_DEVICE_TABLE
        schema.get("RlLocation")?.className = DB_LOCATION_TABLE
        schema.get("RlEvent")?.className = DB_EVENT_TABLE
        schema.get("RlSession")?.className = DB_SESSION_TABLE
    }

    override fun hashCode(): Int {
        return SessionRealmMigration.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is SessionRealmMigration
    }
}
