package com.simprints.id.data.db.subject.local

import io.realm.Realm

interface RealmWrapper {
    suspend fun <R> useRealmInstance(block: (Realm) -> R): R
}
