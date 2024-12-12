package com.simprints.infra.recent.user.activity.local

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.recent.user.activity.ProtoRecentUserActivity
import com.simprints.infra.recent.user.activity.local.RecentUserActivitySharedPrefsMigration.Companion.ENROLMENTS_KEY
import com.simprints.infra.recent.user.activity.local.RecentUserActivitySharedPrefsMigration.Companion.IDENTIFICATIONS_KEY
import com.simprints.infra.recent.user.activity.local.RecentUserActivitySharedPrefsMigration.Companion.LAST_ACTIVITY_DATE_KEY
import com.simprints.infra.recent.user.activity.local.RecentUserActivitySharedPrefsMigration.Companion.LAST_MAC_ADDRESS_KEY
import com.simprints.infra.recent.user.activity.local.RecentUserActivitySharedPrefsMigration.Companion.LAST_SCANNER_USED_KEY
import com.simprints.infra.recent.user.activity.local.RecentUserActivitySharedPrefsMigration.Companion.LAST_USER_KEY
import com.simprints.infra.recent.user.activity.local.RecentUserActivitySharedPrefsMigration.Companion.VERIFICATIONS_KEY
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RecentUserActivitySharedPrefsMigrationTest {
    private val ctx = mockk<Context>()
    private val editor = mockk<SharedPreferences.Editor>(relaxed = true)
    private val preferences = mockk<SharedPreferences>(relaxed = true) {
        every { edit() } returns editor
    }
    private lateinit var recentUserActivitySharedPrefsMigration: RecentUserActivitySharedPrefsMigration

    @Before
    fun setup() {
        every { ctx.getSharedPreferences(any(), any()) } returns preferences
        recentUserActivitySharedPrefsMigration = RecentUserActivitySharedPrefsMigration(ctx)
    }

    @Test
    fun `should migrate should return true if the recent user activity is the one by default`() = runTest {
        val shouldMigrate =
            recentUserActivitySharedPrefsMigration.shouldMigrate(ProtoRecentUserActivity.getDefaultInstance())
        assertThat(shouldMigrate).isTrue()
    }

    @Test
    fun `should migrate should return false if the recent user activity is not the one by default`() = runTest {
        val proto = ProtoRecentUserActivity.newBuilder().setLastActivityTime(10).build()
        val shouldMigrate = recentUserActivitySharedPrefsMigration.shouldMigrate(proto)
        assertThat(shouldMigrate).isFalse()
    }

    @Test
    fun `migrate should work correctly`() = runTest {
        every { preferences.getString(LAST_USER_KEY, "") } returns "user"
        every { preferences.getString(LAST_SCANNER_USED_KEY, "") } returns "scanner"
        every { preferences.getString(LAST_MAC_ADDRESS_KEY, "") } returns "version"
        every { preferences.getLong(LAST_ACTIVITY_DATE_KEY, 0) } returns 50
        every { preferences.getInt(ENROLMENTS_KEY, 0) } returns 10
        every { preferences.getInt(IDENTIFICATIONS_KEY, 0) } returns 20
        every { preferences.getInt(VERIFICATIONS_KEY, 0) } returns 30

        val expectedMigratedActivity = ProtoRecentUserActivity
            .newBuilder()
            .setLastUserUsed("user")
            .setLastScannerUsed("scanner")
            .setLastScannerVersion("version")
            .setEnrolmentsToday(10)
            .setIdentificationsToday(20)
            .setVerificationsToday(30)
            .setLastActivityTime(50)
            .build()

        val migratedActivity =
            recentUserActivitySharedPrefsMigration.migrate(ProtoRecentUserActivity.getDefaultInstance())
        assertThat(migratedActivity).isEqualTo(expectedMigratedActivity)
    }

    @Test
    fun `cleanUp should do remove all the keys`() = runTest {
        every { editor.remove(any()) } returns editor

        recentUserActivitySharedPrefsMigration.cleanUp()

        verify(exactly = 1) { editor.remove(LAST_USER_KEY) }
        verify(exactly = 1) { editor.remove(LAST_SCANNER_USED_KEY) }
        verify(exactly = 1) { editor.remove(LAST_MAC_ADDRESS_KEY) }
        verify(exactly = 1) { editor.remove(LAST_ACTIVITY_DATE_KEY) }
        verify(exactly = 1) { editor.remove(ENROLMENTS_KEY) }
        verify(exactly = 1) { editor.remove(IDENTIFICATIONS_KEY) }
        verify(exactly = 1) { editor.remove(VERIFICATIONS_KEY) }
        verify(exactly = 1) { editor.apply() }
    }
}
