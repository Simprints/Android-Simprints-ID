package com.simprints.infra.config.local.migrations

import android.content.SharedPreferences
import androidx.datastore.core.DataMigration
import com.simprints.infra.config.local.models.ProtoProjectConfiguration
import com.simprints.infra.login.LoginManager
import javax.inject.Inject

/**
 * Can be removed once all the devices have been updated to 2022.3.0
 */
internal class ProjectConfigSharedPrefsMigration @Inject constructor(
    private val loginManager: LoginManager,
    private val sharedPreferences: SharedPreferences,
) : DataMigration<ProtoProjectConfiguration> {

    override suspend fun cleanUp() {
        TODO("Not yet implemented")
    }

    override suspend fun migrate(currentData: ProtoProjectConfiguration): ProtoProjectConfiguration {
        TODO("Not yet implemented")
    }

    override suspend fun shouldMigrate(currentData: ProtoProjectConfiguration): Boolean =
        loginManager.signedInProjectId.isNotEmpty() && currentData.projectId.isEmpty()

    companion object {
        private const val PROJECT_SETTINGS_JSON_STRING_KEY = "ProjectSettingsJsonString"
    }
}
