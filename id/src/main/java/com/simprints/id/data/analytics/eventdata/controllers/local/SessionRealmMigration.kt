package com.simprints.id.data.analytics.eventdata.controllers.local

import com.simprints.id.data.analytics.eventdata.models.local.*
import io.realm.DynamicRealm
import io.realm.RealmMigration
import io.realm.annotations.RealmModule

internal class SessionRealmMigration : RealmMigration {

    //StopShip: classes renamed. Migration Required.
    @RealmModule(classes = [DbDatabaseInfo::class, DbDevice::class, DbLocation::class, DbEvent::class, DbSession::class])
    class SessionModule

    companion object {
        const val REALM_SCHEMA_VERSION: Long = 3
    }

    override fun migrate(realm: DynamicRealm?, oldVersion: Long, newVersion: Long) {}

    override fun hashCode(): Int {
        return SessionRealmMigration.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is SessionRealmMigration
    }
}
