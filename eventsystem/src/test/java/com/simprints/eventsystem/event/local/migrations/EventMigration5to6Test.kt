package com.simprints.eventsystem.event.local.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.randomUUID
import com.simprints.eventsystem.EventSystemApplication
import com.simprints.eventsystem.event.domain.models.ConnectivitySnapshotEvent
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEvent
import com.simprints.eventsystem.event.local.EventRoomDatabase
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = EventSystemApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class EventMigration5to6Test {

    // Array of all migrations
    private val allMigrations = arrayOf(
        EventMigration1to2(),
        EventMigration2to3(),
        EventMigration3to4(),
        EventMigration4to5(),
        EventMigration5to6()
    )

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EventRoomDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun validateMigrationForFingerprintCaptureIsSuccessful() {
        val eventId = randomUUID()

        setupV6DbWithEvent(eventId)

        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true, EventMigration5to6())
        val eventJson = MigrationTestingTools.retrieveCursorWithEventById(db, eventId)
            .getStringWithColumnName("eventJson")!!
        val event = JsonHelper.fromJson(eventJson, object : TypeReference<Event>() {})
        val payloadJsonObject = JSONObject(eventJson).getJSONObject("payload")
        val fingerprintObject = payloadJsonObject.getJSONObject("fingerprint")

        assertThat(event).isInstanceOf(ConnectivitySnapshotEvent::class.java)
        assertThat(event.payload.eventVersion).isEqualTo(3)
        assertThat(fingerprintObject.get("template")).isNull()
        assertThat(fingerprintObject.has("template")).isFalse()
    }

    private fun setupV6DbWithEvent(
        vararg eventId: String, close: Boolean = true
    ): SupportSQLiteDatabase = helper.createDatabase(TEST_DB, 3).apply {
        eventId.forEach {
            val event = createFingerprintCaptureEvent(it)
            this.insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, event)
        }
        if (close) close()
    }

    private fun createFingerprintCaptureEvent(id: String) = ContentValues().apply {
        this.put("id", id)
        this.put("type", "FINGERPRINT_CAPTURE_EVENT")

        val event = FingerprintCaptureEvent(
            id = id, labels = EventLabels(
                projectId = "TEST6Oai41ps1pBNrzBL"
            ), payload = FingerprintCaptureEvent.FingerprintCapturePayload(
                createdAt = 1611584017198,
                eventVersion = 2,
                endedAt = 0,
                finger = IFingerIdentifier.LEFT_3RD_FINGER,
                qualityThreshold = 0,
                result = FingerprintCaptureEvent.FingerprintCapturePayload.Result.GOOD_SCAN,
                fingerprint = FingerprintCaptureEvent.FingerprintCapturePayload.Fingerprint(
                    finger = IFingerIdentifier.LEFT_3RD_FINGER,
                    quality = 0,
                    template = SOME_TEMPLATE
                ),
                id = id,
                type = EventType.FINGERPRINT_CAPTURE
            ), type = EventType.FINGERPRINT_CAPTURE
        )
        this.put("eventJson", JsonHelper.toJson(event))
        this.put("createdAt", 1611584017198)
        this.put("endedAt", 0)
        this.put("sessionIsClosed", 0)
    }

    companion object {
        private const val TEST_DB = "test"
        private const val SOME_TEMPLATE = "some_template"
    }
}

