package com.simprints.infra.realm

import io.realm.Realm

interface RealmWrapper {
    suspend fun <R> useRealmInstance(block: (Realm) -> R): R
}
