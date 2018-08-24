package com.simprints.id.data.analytics.eventData.realm

import com.simprints.id.data.analytics.eventData.models.session.DatabaseInfo
import com.simprints.id.data.analytics.eventData.models.session.Device
import com.simprints.id.data.analytics.eventData.models.session.Location
import io.realm.DynamicRealm
import io.realm.RealmMigration
import io.realm.annotations.RealmModule

internal class SessionRealmMigration : RealmMigration {

    @RealmModule(classes = [DatabaseInfo::class, Device::class, Location::class, RlEvent::class, RlSession::class])
    class SessionModule

    companion object {
        const val REALM_SCHEMA_VERSION: Long = 0
    }

    override fun migrate(realm: DynamicRealm?, oldVersion: Long, newVersion: Long) {}

    override fun hashCode(): Int {
        return SessionRealmMigration.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is SessionRealmMigration
    }
}
