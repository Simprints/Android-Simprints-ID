package com.simprints.id.services.scheduledSync.peopleDownSync.controllers

import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope

interface SyncScopesBuilder {

    fun fromJsonToSyncScope(json: String): SyncScope?
    fun fromSyncScopeToJson(syncScope: SyncScope): String?

    fun fromJsonToSubSyncScope(json: String): SubSyncScope?
    fun fromSubSyncScopeToJson(syncScope: SubSyncScope): String?
    fun buildSyncScope(): SyncScope?
}
