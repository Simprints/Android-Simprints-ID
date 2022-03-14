package com.simprints.eventsystem.event.local.migrations

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.simprints.eventsystem.EventSystemApplication
import com.simprints.eventsystem.event.local.EventRoomDatabase
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = EventSystemApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class EventMigration5to6Test {

    private val testDb = "test"

    // Array of all migrations
    private val allMigrations = arrayOf(
        EventMigration1to2(), EventMigration2to3(), EventMigration3to4(), EventMigration4to5(), EventMigration5to6())

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EventRoomDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )
}
