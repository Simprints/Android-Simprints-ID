package com.simprints.id.data.db.event.local

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase.CONFLICT_NONE
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.randomUUID
import com.simprints.id.data.db.event.domain.models.EnrolmentEventV1
import com.simprints.id.data.db.event.domain.models.EnrolmentEventV2
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT_V1
import com.simprints.id.data.db.event.local.models.fromDbToDomain
import com.simprints.id.testtools.TestApplication
import com.simprints.id.tools.extensions.getStringWithColumnName
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.mockk
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class EventMigration1to2Test {

    @get:Rule
    public val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        "${EventRoomDatabase::class.java.canonicalName}",
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        val id = randomUUID()

        var db = helper.createDatabase(TEST_DB, 1).apply {
            val values = ContentValues()
            values.put("id", id)
            values.put("type", "ENROLMENT")
            val unversionedEnrolmentEvent = "{\"id\":\"$id\",\"labels\":{\"projectId\":\"TEST6Oai41ps1pBNrzBL\",\"sessionId\":\"e35c39f9-b81e-48f2-97e7-46ecc8399bb4\",\"deviceId\":\"f2fd8393c0a0be67\"},\"payload\":{\"createdAt\":1611584017198,\"eventVersion\":1,\"personId\":\"61881de4-22f2-4e13-861a-21a209db8581\",\"type\":\"ENROLMENT_V1\",\"endedAt\":0},\"type\":\"ENROLMENT_V1\"}"
            values.put("eventJson", unversionedEnrolmentEvent)
            values.put("createdAt", 0)
            values.put("endedAt", 0)

            this.insert("DbEvent", CONFLICT_NONE, values)
            close()
        }

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, EventMigration1to2(mockk()))

        val cursor = retrieveCursorWithEventById(db, id)
        validateEventType(cursor)
        val eventJson = cursor.getStringWithColumnName("eventJson")!!
        validatePayloadType(eventJson, cursor)
    }

    private fun validateEventType(cursor: Cursor) {
        assertThat(cursor.getStringWithColumnName("type")).isEqualTo(ENROLMENT_V1.toString())
    }

    private fun validatePayloadType(eventJson: String, cursor: Cursor) {
        val enrolmentEventV2 = JsonHelper().fromJson(eventJson, object : TypeReference<Event>() {})
        assertThat(enrolmentEventV2).isInstanceOf(EnrolmentEventV1::class.java)
    }

    private fun retrieveCursorWithEventById(db: SupportSQLiteDatabase, id: String): Cursor =
        db.query("SELECT * from DbEvent where id= ?", arrayOf(id)).apply { moveToNext() }

    companion object {
        private const val TEST_DB = "test"
    }
}
