package com.simprints.infra.enrolment.records.realm.store

import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm

interface RealmWrapper {
    /**
     * Returns read-only Realm instance for data fetching.
     */
    suspend fun <R> readRealm(block: (Realm) -> R): R

    /**
     * Executes provided block with a writable Realm instance ensuring
     * that modifications are handled in a transaction.
     */
    suspend fun <R> writeRealm(block: (MutableRealm) -> R)
}
