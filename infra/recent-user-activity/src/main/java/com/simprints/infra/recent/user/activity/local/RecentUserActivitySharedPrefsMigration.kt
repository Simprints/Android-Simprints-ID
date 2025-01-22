package com.simprints.infra.recent.user.activity.local

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataMigration
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.recent.user.activity.ProtoRecentUserActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Can be removed once all the devices have been updated to 2022.3.0
 */
internal class RecentUserActivitySharedPrefsMigration @Inject constructor(
    @ApplicationContext ctx: Context,
) : DataMigration<ProtoRecentUserActivity> {
    private val prefs = ctx.getSharedPreferences(PREF_FILE_NAME, PREF_MODE)

    override suspend fun cleanUp() {
        prefs
            .edit()
            .remove(ENROLMENTS_KEY)
            .remove(IDENTIFICATIONS_KEY)
            .remove(VERIFICATIONS_KEY)
            .remove(LAST_ACTIVITY_DATE_KEY)
            .remove(LAST_USER_KEY)
            .remove(LAST_SCANNER_USED_KEY)
            .remove(LAST_MAC_ADDRESS_KEY)
            .apply()
        Simber.i("Migration of recent user activity to Datastore done", tag = MIGRATION)
    }

    override suspend fun migrate(currentData: ProtoRecentUserActivity): ProtoRecentUserActivity {
        Simber.i("Start migration of recent user activity to Datastore", tag = MIGRATION)
        return currentData
            .toBuilder()
            .setEnrolmentsToday(prefs.getInt(ENROLMENTS_KEY, 0))
            .setIdentificationsToday(prefs.getInt(IDENTIFICATIONS_KEY, 0))
            .setVerificationsToday(prefs.getInt(VERIFICATIONS_KEY, 0))
            .setLastActivityTime(prefs.getLong(LAST_ACTIVITY_DATE_KEY, 0))
            .setLastUserUsed(prefs.getString(LAST_USER_KEY, ""))
            .setLastScannerUsed(prefs.getString(LAST_SCANNER_USED_KEY, ""))
            .setLastScannerVersion(prefs.getString(LAST_MAC_ADDRESS_KEY, ""))
            .build()
    }

    override suspend fun shouldMigrate(currentData: ProtoRecentUserActivity): Boolean =
        currentData == ProtoRecentUserActivity.getDefaultInstance()

    companion object {
        private const val PREF_FILE_NAME = "b3f0cf9b-4f3f-4c5b-bf85-7b1f44eddd7a"
        private const val PREF_MODE = Context.MODE_PRIVATE

        @VisibleForTesting
        const val LAST_USER_KEY = "LastUserUsed"

        @VisibleForTesting
        const val LAST_SCANNER_USED_KEY = "LastScannerUsed"

        @VisibleForTesting
        const val LAST_MAC_ADDRESS_KEY = "LastMacAddress"

        @VisibleForTesting
        const val ENROLMENTS_KEY = "Enrolments"

        @VisibleForTesting
        const val IDENTIFICATIONS_KEY = "Identifications"

        @VisibleForTesting
        const val VERIFICATIONS_KEY = "Verifications"

        @VisibleForTesting
        const val LAST_ACTIVITY_DATE_KEY = "LastActivityDate"
    }
}
