package com.simprints.infra.events.event.local.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.events.event.local.EventRoomDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class EventMigration17to18Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EventRoomDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun `validate migration creates index on DbEvent`() {
        setupV17DbWithData()

        val db = helper.runMigrationsAndValidate(TEST_DB, 18, true, EventMigration17to18())

        val cursor = db.query(
            "SELECT name FROM sqlite_master WHERE type = 'index' AND name = 'index_DbEvent_scopeId_createdAt_unixMs'",
        )
        cursor.use { assertThat(it.count).isEqualTo(1) }

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun `validate migration creates index on DbEventScope`() {
        setupV17DbWithData()

        val db = helper.runMigrationsAndValidate(TEST_DB, 18, true, EventMigration17to18())

        val cursor = db.query(
            "SELECT name FROM sqlite_master WHERE type = 'index' AND name = 'index_DbEventScope_type_start_unixMs'",
        )
        cursor.use { assertThat(it.count).isEqualTo(1) }

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun `validate existing data is preserved after migration`() {
        val eventId = randomUUID()
        val scopeId = randomUUID()
        setupV17DbWithData(eventId = eventId, scopeId = scopeId)

        val db = helper.runMigrationsAndValidate(TEST_DB, 18, true, EventMigration17to18())

        val eventCursor = db.query("SELECT id FROM DbEvent WHERE id = '$eventId'")
        eventCursor.use { assertThat(it.count).isEqualTo(1) }

        val scopeCursor = db.query("SELECT id FROM DbEventScope WHERE id = '$scopeId'")
        scopeCursor.use { assertThat(it.count).isEqualTo(1) }

        db.close()
    }

    private fun setupV17DbWithData(
        eventId: String = randomUUID(),
        scopeId: String = randomUUID(),
    ) = helper.createDatabase(TEST_DB, 17).apply {
        insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, createEventValues(eventId, scopeId))
        insert("DbEventScope", SQLiteDatabase.CONFLICT_NONE, createScopeValues(scopeId))
        close()
    }

    private fun createEventValues(
        id: String,
        scopeId: String,
    ) = ContentValues().apply {
        put("id", id)
        put("type", "INTENT_PARSING")
        put("createdAt_unixMs", 1000L)
        put("createdAt_isTrustworthy", 0)
        put("projectId", "testProject")
        put("scopeId", scopeId)
        put("eventJson", """{"id":"$id","type":"INTENT_PARSING"}""")
    }

    private fun createScopeValues(id: String) = ContentValues().apply {
        put("id", id)
        put("projectId", "testProject")
        put("type", "SESSION")
        put("start_unixMs", 1000L)
        put("start_isTrustworthy", 0)
        put("end_unixMs", 2000L)
        put("end_isTrustworthy", 0)
        put("payloadJson", """{}""")
    }

    companion object {
        private const val TEST_DB = "test"
    }
}
