package com.simprints.infra.config.store.local.migrations

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataMigration
import com.fasterxml.jackson.core.JacksonException
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.local.migrations.models.OldProjectConfig
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import com.simprints.infra.config.store.local.models.toProto
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Can be removed once all the devices have been updated to 2022.4.0
 */
internal class ProjectConfigSharedPrefsMigration @Inject constructor(
    @ApplicationContext ctx: Context,
    private val authStore: AuthStore,
) : DataMigration<ProtoProjectConfiguration> {
    private val prefs = ctx.getSharedPreferences(PREF_FILE_NAME, PREF_MODE)

    override suspend fun cleanUp() {
        val editor = prefs.edit()
        ALL_KEYS.forEach { editor.remove(it) }
        editor.remove(PROJECT_SETTINGS_JSON_STRING_KEY)
        editor.apply()
        Simber.i("Migration of project configuration to Datastore done", tag = MIGRATION)
    }

    override suspend fun migrate(currentData: ProtoProjectConfiguration): ProtoProjectConfiguration {
        Simber.i("Start migration of project configuration to Datastore", tag = MIGRATION)
        val projectSettingsJson = prefs.getString(PROJECT_SETTINGS_JSON_STRING_KEY, "")
        if (projectSettingsJson.isNullOrEmpty()) return currentData

        return try {
            JsonHelper
                .fromJson<OldProjectConfig>(projectSettingsJson)
                .toDomain(authStore.signedInProjectId)
                .toProto()
        } catch (e: Exception) {
            if (e is JacksonException) {
                // Return default value
                Simber.i("Invalid old configuration for project ${authStore.signedInProjectId}", e, tag = MIGRATION)
                ProtoProjectConfiguration.getDefaultInstance()
            } else {
                Simber.e("Failed to migrate project configuration to Datastore", e, tag = MIGRATION)
                throw e
            }
        }
    }

    override suspend fun shouldMigrate(currentData: ProtoProjectConfiguration): Boolean = prefs
        .getString(PROJECT_SETTINGS_JSON_STRING_KEY, "")
        ?.isNotEmpty() == true &&
        currentData.projectId.isEmpty()

    companion object {
        @VisibleForTesting
        const val PROJECT_SETTINGS_JSON_STRING_KEY = "ProjectSettingsJsonString"
        private const val PREF_FILE_NAME = "b3f0cf9b-4f3f-4c5b-bf85-7b1f44eddd7a"
        private const val PREF_MODE = Context.MODE_PRIVATE

        @VisibleForTesting
        val ALL_KEYS = listOf(
            "NbOfIdsInt",
            "ProjectLanguages",
            "ModuleIdOptions",
            "MaxNbOfModules",
            "SyncGroup",
            "MatchGroup",
            "ProgramName",
            "OrganizationName",
            "ConsentParentalExists",
            "ConsentGeneralOptions",
            "ConsentParentalOptions",
            "LogoExists",
            "ConsentRequired",
            "LocationRequired",
            "EnrolmentPlus",
            "DownSyncSetting",
            "SimprintsSync",
            "CoSync",
            "Modality",
            "FingerImagesExist",
            "CaptureFingerprintStrategy",
            "FingerComparisonStrategyForVerification",
            "SaveFingerprintImagesStrategy",
            "ScannerGenerations",
            "FingerprintLiveFeedbackOn",
            "SaveFaceImages",
            "FingerprintQualityThreshold",
            "FaceQualityThreshold",
            "FaceNbOfFramesCaptured",
            "FingerprintConfidenceThresholds",
            "FaceConfidenceThresholds",
            "Vero2FirmwareVersions",
        )
    }
}
