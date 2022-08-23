package com.simprints.infra.config.local.migrations

import android.content.Context
import androidx.datastore.core.DataMigration
import com.simprints.infra.config.local.models.ProtoDeviceConfiguration
import com.simprints.infra.logging.Simber
import javax.inject.Inject

/**
 * Can be removed once all the devices have been updated to 2022.3.0
 */
internal class DeviceConfigSharedPrefsMigration @Inject constructor(
    ctx: Context,
) : DataMigration<ProtoDeviceConfiguration> {

    private val prefs = ctx.getSharedPreferences(PREF_FILE_NAME, PREF_MODE)

    override suspend fun cleanUp() {
        Simber.i("Migration of device configuration to Datastore done")
    }

    override suspend fun migrate(currentData: ProtoDeviceConfiguration): ProtoDeviceConfiguration {
        Simber.i("Start migration of device configuration to Datastore")
        val language = prefs.getString("", "")
        if (language.isNullOrEmpty()) return currentData
        return currentData.toBuilder().setLanguage(language).build()
    }

    override suspend fun shouldMigrate(currentData: ProtoDeviceConfiguration): Boolean =
        currentData.language.isEmpty()

    companion object {
        private const val PREF_FILE_NAME = "b3f0cf9b-4f3f-4c5b-bf85-7b1f44eddd7a"
        private const val PREF_MODE = Context.MODE_PRIVATE
    }
}
