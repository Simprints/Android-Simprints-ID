package com.simprints.infra.events.event.local.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.events.event.local.EventRoomDatabase
import io.mockk.spyk
import io.mockk.verify
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class EventMigration16to17Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EventRoomDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun `validate end to end migration is successful`() {
        val eventId = randomUUID()

        setupV16DbWithEvent(eventId)

        val db = helper.runMigrationsAndValidate(TEST_DB, 17, true, EventMigration16to17())

        val eventJson = MigrationTestingTools
            .retrieveCursorWithEventById(db, eventId)
            .getStringWithColumnName("eventJson")!!

        val jsonObject = JSONObject(eventJson)
        val payload = jsonObject.getJSONObject("payload")
        assertThat(eventJson).contains("\"$EXTERNAL_CREDENTIAL_IDS_JSON_KEY\":[]")
        assertThat(payload.has(EXTERNAL_CREDENTIAL_IDS_JSON_KEY)).isTrue()
        assertThat(payload.getJSONArray(EXTERNAL_CREDENTIAL_IDS_JSON_KEY).length()).isEqualTo(0)

        db.close()
    }

    @Test
    fun `validate migration is called`() {
        val migrationSpy = spyk(EventMigration16to17())

        setupV16DbWithEvent(randomUUID())
        helper.runMigrationsAndValidate(TEST_DB, 17, true, migrationSpy)

        verify(exactly = 1) { migrationSpy.migrate(any<SupportSQLiteDatabase>()) }
    }

    @Test
    fun `validate all ENROLMENT_V4 events are migrated`() {
        val eventId1 = randomUUID()
        val eventId2 = randomUUID()

        setupV16DbWithEvent(eventId1, eventId2)
        val db = helper.runMigrationsAndValidate(TEST_DB, 17, true, EventMigration16to17())

        MigrationTestingTools.retrieveCursorWithEventById(db, eventId1).use { cursor ->
            val eventJson = cursor.getStringWithColumnName("eventJson")
            assertThat(eventJson).contains("\"$EXTERNAL_CREDENTIAL_IDS_JSON_KEY\":[]")
        }

        MigrationTestingTools.retrieveCursorWithEventById(db, eventId2).use { cursor ->
            val eventJson = cursor.getStringWithColumnName("eventJson")
            assertThat(eventJson).contains("\"$EXTERNAL_CREDENTIAL_IDS_JSON_KEY\":[]")
        }

        db.close()
    }

    @Test
    fun `validate migration query is called`() {
        val migrationSpy = spyk(EventMigration16to17())

        val db = spyk(setupV16DbWithEvent(randomUUID(), close = false))
        migrationSpy.migrate(db)

        verify(atLeast = 1) { db.query(any<SupportSQLiteQuery>()) }
        db.close()
    }

    @Test
    fun `validate migration does not add field if it already exists`() {
        val eventId = randomUUID()

        setupV16DbWithEventWithExternalCredentialIds(eventId)
        val db = helper.runMigrationsAndValidate(TEST_DB, 17, true, EventMigration16to17())

        val eventJson = MigrationTestingTools
            .retrieveCursorWithEventById(db, eventId)
            .getStringWithColumnName("eventJson")!!

        // Verify the field appears only once
        val firstIndex = eventJson.indexOf("\"$EXTERNAL_CREDENTIAL_IDS_JSON_KEY\"")
        val lastIndex = eventJson.lastIndexOf("\"$EXTERNAL_CREDENTIAL_IDS_JSON_KEY\"")
        assertThat(firstIndex).isEqualTo(lastIndex)
        assertThat(eventJson).contains(EXTERNAL_CREDENTIAL_IDS_JSON_FIELD)

        db.close()
    }

    @Test
    fun `validate non-ENROLMENT_V4 events are not modified`() {
        val eventId = randomUUID()

        setupV16DbWithNonEnrolmentEvent(eventId)
        val db = helper.runMigrationsAndValidate(TEST_DB, 17, true, EventMigration16to17())

        val eventJson = MigrationTestingTools
            .retrieveCursorWithEventById(db, eventId)
            .getStringWithColumnName("eventJson")!!

        assertThat(eventJson).doesNotContain(EXTERNAL_CREDENTIAL_IDS_JSON_KEY)
        db.close()
    }

    private fun createEnrolmentEvent(
        id: String,
        addCredentialIds: Boolean,
    ) = ContentValues().apply {
        val externalCredentialIds = if (addCredentialIds) {
            EXTERNAL_CREDENTIAL_IDS_JSON_FIELD
        } else {
            ""
        }
        put("id", id)
        put("type", "ENROLMENT_V4")
        put("createdAt_unixMs", 123)
        put("createdAt_isTrustworthy", 0)
        put("projectId", "9WNCAbWVNrxttDe5hgwb")
        put("scopeId", "2bdc1145")
        val unversionedEnrolmentEvent =
            """
            {
               "id":"$id",
               "payload":{
                  "createdAt":{
                     "ms":1762805893067,
                     "isTrustworthy":true,
                     "msSinceBoot":35002538
                  },
                  "eventVersion":4,
                  "subjectId":"74639420-8e77-4a40-a452-280f295f147f",
                  "projectId":"FW1jU2kjy1cV9RWXdosN",
                  "moduleId":{
                     "className":"TokenizableString.Tokenized",
                     "value":"AV50RNsaMs9jpoHwcXZqir1uB3St0vsexOpixA=="
                  },
                  "attendantId":{
                     "className":"TokenizableString.Tokenized",
                     "value":"AQYk7uBNIkhgOVGR3f/0HTjX/LRk0fKi+g=="
                  },
                  "biometricReferenceIds":[
                     "b12815ff-a4bc-4d7f-ae88-608640c7138d"
                  ],
                  $externalCredentialIds
                  "type":"ENROLMENT_V4"
               },
               "type":"ENROLMENT_V4",
               "scopeId":"c33023a1-d335-4310-b088-81575daafea3",
               "projectId":"FW1jU2kjy1cV9RWXdosN"
            }
            """.trimIndent()
        put("eventJson", unversionedEnrolmentEvent)
    }

    private fun createNonEnrolmentEvent(id: String) = ContentValues().apply {
        put("id", id)
        put("type", "INTENT_PARSING")
        put("createdAt_unixMs", 123)
        put("createdAt_isTrustworthy", 0)
        put("projectId", "9WNCAbWVNrxttDe5hgwb")
        put("scopeId", "2bdc1145")
        put(
            "eventJson",
            """
            {
               "id":"d256e644-ce5b-4ec5-8909-3a372a930206",
               "projectId":"9WNCAbWVNrxttDe5hgwb",
               "sessionId":"2bdc1145-cbec-4e6a-ac8a-61c1e5b53bb4",
               "payload":{
                  "createdAt":{
                     "ms":1706534485916,
                     "isTrustworthy":false,
                     "msSinceBoot":null
                  },
                  "eventVersion":2,
                  "integration":"STANDARD",
                  "type":"INTENT_PARSING",
                  "endedAt":{
                     "ms":1706534528165,
                     "isTrustworthy":false,
                     "msSinceBoot":null
                  }
               },
               "type":"INTENT_PARSING"
            }
            """.trimIndent(),
        )
    }

    private fun setupV16DbWithEvent(
        vararg eventId: String,
        close: Boolean = true,
    ): SupportSQLiteDatabase = helper.createDatabase(TEST_DB, 16).apply {
        eventId.forEach { id ->
            val event = createEnrolmentEvent(id, addCredentialIds = false)
            this.insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, event)
        }
        if (close) {
            close()
        }
    }

    private fun setupV16DbWithEventWithExternalCredentialIds(
        eventId: String,
        close: Boolean = true,
    ): SupportSQLiteDatabase = helper.createDatabase(TEST_DB, 16).apply {
        val event = createEnrolmentEvent(eventId, addCredentialIds = true)
        this.insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, event)
        if (close) {
            close()
        }
    }

    private fun setupV16DbWithNonEnrolmentEvent(
        eventId: String,
        close: Boolean = true,
    ): SupportSQLiteDatabase = helper.createDatabase(TEST_DB, 16).apply {
        val event = createNonEnrolmentEvent(eventId)
        this.insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, event)
        if (close) {
            close()
        }
    }

    companion object {
        private const val TEST_DB = "test"
        private const val EXTERNAL_CREDENTIAL_IDS_JSON_KEY = "externalCredentialIds"
        private const val EXTERNAL_CREDENTIAL_IDS_JSON_FIELD =
            "\"$EXTERNAL_CREDENTIAL_IDS_JSON_KEY\": [\"74639420-8e77-4a40-a452-280f295f147f\"]\","
    }
}
