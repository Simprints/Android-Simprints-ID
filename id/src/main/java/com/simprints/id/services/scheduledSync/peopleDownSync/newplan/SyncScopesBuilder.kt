package com.simprints.id.services.scheduledSync.peopleDownSync.newplan

interface SyncScopesBuilder {

    fun fromJsonToSyncScope(json: String): SyncScope?
    fun fromSyncScopeToJson(syncScope: SyncScope): String?

    fun fromJsonToSubSyncScope(json: String): SubSyncScope?
    fun fromSubSyncScopeToJson(syncScope: SubSyncScope): String?
    fun buildSyncScope(): SyncScope?
}
