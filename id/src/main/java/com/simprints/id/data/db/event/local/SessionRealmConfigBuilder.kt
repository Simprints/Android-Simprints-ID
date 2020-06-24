package com.simprints.id.data.db.event.local

import io.realm.RealmConfiguration

interface SessionRealmConfigBuilder {

    fun build(databaseName: String, key: ByteArray): RealmConfiguration
}
