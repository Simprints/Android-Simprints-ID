package com.simprints.infra.events.event.local.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.infra.events.event.local.EventRoomDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventMigration9To10Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EventRoomDatabase::class.java,
    )

    @Test
    fun `when event contains moduleId and attendantId values in event JSON, should refactor the values to match TokenizedString`() {
        val eventFromV9 = createEvent(json = EVENT_JSON_OLD)
        helper.createDatabase(TEST_DB, 9).apply {
            insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, eventFromV9)
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 10, true, EventMigration9to10())

        // validate that the JSON values were migrated properly.
        val event = MigrationTestingTools.retrieveCursorWithEventById(db, EVENT_ID)
        val eventJson = event.getStringWithColumnName(EVENT_JSON_FIELD)

        assertThat(eventJson).isEqualTo(EVENT_JSON_NEW)
    }

    private fun createEvent(json: String) = ContentValues().apply {
        put("id", EVENT_ID)
        put("type", "ENROLMENT_V2")
        put(EVENT_JSON_FIELD, json)
        put("createdAt", 12345)
        put("endedAt", 12445)
        put("sessionIsClosed", 0)
    }

    companion object {
        private const val TEST_DB = "some_db"
        private const val EVENT_ID = "some-event-id"
        private const val EVENT_JSON_FIELD = "eventJson"
        private const val EVENT_JSON_OLD =
            "{\"id\":\"id\",\"labels\":{\"projectId\":\"projectId\",\"sessionId\":\"sessionId\",\"deviceId\":\"deviceId\"},\"payload\":{\"payload\":{\"createdAt\":0,\"eventVersion\":2,\"subjectId\":\"subjectId\",\"projectId\":\"projectId\",\"moduleId\":\"moduleId\",\"attendantId\":\"attendantId\",\"personCreationEventId\":\"personCreationEventId\",\"type\":\"ENROLMENT_V2\",\"endedAt\":0},\"param1\":\"param1\",\"param2\":94,\"createdAt\":0,\"eventVersion\":2,\"type\":\"ENROLMENT_V2\",\"endedAt\":0},\"type\":\"ENROLMENT_V2\"}"
        private const val EVENT_JSON_NEW =
            "{\"id\":\"id\",\"labels\":{\"projectId\":\"projectId\",\"sessionId\":\"sessionId\",\"deviceId\":\"deviceId\"},\"payload\":{\"payload\":{\"createdAt\":0,\"eventVersion\":2,\"subjectId\":\"subjectId\",\"projectId\":\"projectId\",\"moduleId\":{\"className\":\"TokenizableString.Raw\",\"value\":\"moduleId\"},\"attendantId\":{\"className\":\"TokenizableString.Raw\",\"value\":\"attendantId\"},\"personCreationEventId\":\"personCreationEventId\",\"type\":\"ENROLMENT_V2\",\"endedAt\":0},\"param1\":\"param1\",\"param2\":94,\"createdAt\":0,\"eventVersion\":2,\"type\":\"ENROLMENT_V2\",\"endedAt\":0},\"type\":\"ENROLMENT_V2\"}"
    }
}
