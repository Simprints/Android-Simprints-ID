package com.simprints.id.data.db.session.local

import com.simprints.id.data.db.session.local.models.*
import com.simprints.id.data.db.session.local.oldschemas.SessionEventsSchemaV3
import io.realm.DynamicRealm
import io.realm.RealmMigration
import io.realm.RealmSchema
import io.realm.annotations.RealmModule

//TODO: Delete it - not required. We delete the db if migration is needed.
internal class SessionRealmMigration : RealmMigration {

    @RealmModule(classes = [DbDatabaseInfo::class, DbDevice::class, DbLocation::class, DbEvent::class, DbSession::class])
    class SessionModule

    companion object {
        const val REALM_SCHEMA_VERSION: Long = 5
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
        schema.get(SessionEventsSchemaV3.DB_INFO_TABLE)?.className = DB_INFO_TABLE
        schema.get(SessionEventsSchemaV3.DB_DEVICE_TABLE)?.className = DB_DEVICE_TABLE
        schema.get(SessionEventsSchemaV3.DB_LOCATION_TABLE)?.className = DB_LOCATION_TABLE
        schema.get(SessionEventsSchemaV3.DB_EVENT_TABLE)?.className = DB_EVENT_TABLE
        schema.get(SessionEventsSchemaV3.DB_SESSION_TABLE)?.className = DB_SESSION_TABLE
    }

    override fun hashCode(): Int {
        return SessionRealmMigration.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is SessionRealmMigration
    }
}
