package com.simprints.infra.config.local.migrations

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataMigration
import com.simprints.infra.config.domain.models.Finger
import com.simprints.infra.config.local.models.ProtoDeviceConfiguration
import com.simprints.infra.config.local.models.toProto
import com.simprints.infra.logging.Simber
import com.simprints.infra.login.LoginManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Can be removed once all the devices have been updated to 2022.4.0
 */
internal class DeviceConfigSharedPrefsMigration @Inject constructor(
    @ApplicationContext ctx: Context,
    private val loginManager: LoginManager,
) : DataMigration<ProtoDeviceConfiguration> {

    private val prefs = ctx.getSharedPreferences(PREF_FILE_NAME, PREF_MODE)

    override suspend fun cleanUp() {
        prefs.edit()
            .remove(LANGUAGE_KEY)
            .remove(LANGUAGE_OVERRIDDEN_KEY)
            .remove(FINGERS_TO_COLLECT_KEY)
            .remove(FINGERS_TO_COLLECT_OVERRIDDEN_KEY)
            .remove(SELECTED_MODULES_KEY)
            .remove(LAST_INSTRUCTION_ID_KEY)
            .apply()
        Simber.i("Migration of device configuration to Datastore done")
    }

    override suspend fun migrate(currentData: ProtoDeviceConfiguration): ProtoDeviceConfiguration {
        Simber.i("Start migration of device configuration to Datastore")
        return currentData.let {
            val proto = it.toBuilder()

            val language = prefs.getString(LANGUAGE_KEY, "")
            if (!language.isNullOrEmpty()) {
                val isOverridden = prefs.getBoolean(LANGUAGE_OVERRIDDEN_KEY, false)
                proto.language = ProtoDeviceConfiguration.Language.newBuilder()
                    .setLanguage(language)
                    .setIsOverwritten(isOverridden)
                    .build()
            }

            val fingersToCollect = prefs.getString(FINGERS_TO_COLLECT_KEY, "")
            if (!fingersToCollect.isNullOrEmpty()) {
                val isOverridden = prefs.getBoolean(FINGERS_TO_COLLECT_OVERRIDDEN_KEY, false)
                proto.fingersToCollect = ProtoDeviceConfiguration.FingersToCollect.newBuilder()
                    .addAllFingersToCollect(
                        fingersToCollect
                            .split(",")
                            .map { finger -> Finger.valueOf(finger).toProto() }
                    )
                    .setIsOverwritten(isOverridden)
                    .build()
            }

            val selectedModules = prefs.getString(SELECTED_MODULES_KEY, "")
            if (!selectedModules.isNullOrEmpty()) {
                proto.addAllModuleSelected(selectedModules.split("|"))
            }

            proto.lastInstructionId = prefs.getString(LAST_INSTRUCTION_ID_KEY, "")
            proto.build()
        }
    }

    override suspend fun shouldMigrate(currentData: ProtoDeviceConfiguration): Boolean =
        loginManager.signedInProjectId.isNotEmpty() && prefs.getString(LANGUAGE_KEY, "") != ""

    companion object {
        private const val PREF_FILE_NAME = "b3f0cf9b-4f3f-4c5b-bf85-7b1f44eddd7a"
        private const val PREF_MODE = Context.MODE_PRIVATE

        @VisibleForTesting
        const val LANGUAGE_KEY = "SelectedLanguage"

        @VisibleForTesting
        const val LANGUAGE_OVERRIDDEN_KEY = "SelectedLanguage_isOverridden"

        @VisibleForTesting
        const val SELECTED_MODULES_KEY = "SelectedModules"

        @VisibleForTesting
        const val FINGERS_TO_COLLECT_KEY = "FingerprintsToCollect"

        @VisibleForTesting
        const val FINGERS_TO_COLLECT_OVERRIDDEN_KEY = "FingerprintsToCollect_isOverridden"

        @VisibleForTesting
        const val LAST_INSTRUCTION_ID_KEY = "LastInstructionId"
    }
}
