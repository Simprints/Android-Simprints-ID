package com.simprints.infra.config.local.migrations

import android.content.Context
import androidx.datastore.core.DataMigration
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.simprints.infra.config.local.migrations.models.OldProjectConfig
import com.simprints.infra.config.local.models.ProtoProjectConfiguration
import com.simprints.infra.config.local.models.toProto
import com.simprints.infra.logging.Simber
import com.simprints.infra.login.LoginManager
import javax.inject.Inject

/**
 * Can be removed once all the devices have been updated to 2022.3.0
 */
internal class ProjectConfigSharedPrefsMigration @Inject constructor(
    ctx: Context,
    private val loginManager: LoginManager,
) : DataMigration<ProtoProjectConfiguration> {

    private val prefs = ctx.getSharedPreferences(PREF_FILE_NAME, PREF_MODE)

    override suspend fun cleanUp() {
        Simber.i("Migration of configuration to Datastore done")
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun migrate(currentData: ProtoProjectConfiguration): ProtoProjectConfiguration {
        Simber.i("Start migration of configuration to Datastore")
        val projectSettingsJson = prefs.getString(PROJECT_SETTINGS_JSON_STRING_KEY, "")
        if (projectSettingsJson.isNullOrEmpty()) return currentData

        return jacksonObjectMapper()
            .readValue(projectSettingsJson, OldProjectConfig::class.java)
            .toDomain(loginManager.signedInProjectId)
            .toProto()
    }

    override suspend fun shouldMigrate(currentData: ProtoProjectConfiguration): Boolean =
        loginManager.signedInProjectId.isNotEmpty() && currentData.projectId.isEmpty()

    companion object {
        private const val PROJECT_SETTINGS_JSON_STRING_KEY = "ProjectSettingsJsonString"
        private const val PREF_FILE_NAME = "b3f0cf9b-4f3f-4c5b-bf85-7b1f44eddd7a"
        private const val PREF_MODE = Context.MODE_PRIVATE
    }
}
