package com.simprints.id.services.scheduledSync.peopleDownSync.controllers

import com.google.gson.Gson
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.Constants
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope

class SyncScopesBuilderImpl(val loginInfoManager: LoginInfoManager,
                            val preferencesManager: PreferencesManager,
                            val gson: Gson = Gson()) : SyncScopesBuilder {

    override fun fromJsonToSyncScope(json: String): SyncScope? = fromJson(json)
    override fun fromSyncScopeToJson(syncScope: SyncScope): String? = toJson(syncScope)
    override fun fromJsonToSubSyncScope(json: String): SubSyncScope? = fromJson(json)
    override fun fromSubSyncScopeToJson(syncScope: SubSyncScope): String? = toJson(syncScope)

    override fun buildSyncScope(): SyncScope? {

        val projectId = loginInfoManager.getSignedInProjectIdOrEmpty()
        var possibleUserId: String? = loginInfoManager.getSignedInUserIdOrEmpty()
        var possibleModuleIds: Set<String>? = preferencesManager.selectedModules

        if (projectId.isEmpty()) return null
        if (possibleUserId.isNullOrEmpty()) return null

        when (preferencesManager.syncGroup) {
            Constants.GROUP.GLOBAL -> {
                possibleUserId = null
                possibleModuleIds = null
            }
            Constants.GROUP.USER -> possibleModuleIds = null
            Constants.GROUP.MODULE -> possibleUserId = null
        }

        return SyncScope(projectId, possibleUserId, possibleModuleIds)
    }

    inline fun <reified T> fromJson(json: String): T? = try {
        gson.fromJson(json, T::class.java)
    } catch (t: Throwable) {
        t.printStackTrace()
        null
    }

    inline fun <reified T> toJson(scope: T): String? = gson.toJson(scope)
}
