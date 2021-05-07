package com.simprints.id.data.db.event.local.migrations

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.randomUUID
import com.simprints.id.data.db.event.domain.models.session.DatabaseInfo
import com.simprints.id.data.db.event.domain.models.session.Device
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.data.db.event.local.EventRoomDatabase
import com.simprints.id.domain.modality.Modes
import com.simprints.id.testtools.TestApplication
import com.simprints.id.tools.extensions.getIntWithColumnName
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class EventMigration2to3Test {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        "${EventRoomDatabase::class.java.canonicalName}",
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate2To3() {
        val openSessionCaptureEventId = randomUUID()
        val closedSessionCaptureEventId = randomUUID()

        @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")
        var db = helper.createDatabase(TEST_DB, 2).apply {
            val openSessionCaptureEvent = createSessionCaptureEvent(openSessionCaptureEventId, 0)
            val closedSessionCaptureEvent =
                createSessionCaptureEvent(closedSessionCaptureEventId, 16115)

            this.insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, openSessionCaptureEvent)
            this.insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, closedSessionCaptureEvent)
            close()
        }

        db = helper.runMigrationsAndValidate(
            TEST_DB, 3, true, EventMigration2to3(mockk())
        )

        validateColumnCreation(db, openSessionCaptureEventId)
        validateOpenSessionIsStillOpen(db, openSessionCaptureEventId)
        validateClosedSessionIsStillClosed(db, closedSessionCaptureEventId)
    }

    private fun createSessionCaptureEvent(id: String, endedAt: Long) = ContentValues().apply {
        this.put("id", id)
        this.put("type", "SESSION_CAPTURE")
        val session = SessionCaptureEvent(
            id = id,
            projectId = "TEST6Oai41ps1pBNrzBL",
            createdAt = 1611584017198,
            modalities = listOf(Modes.FINGERPRINT),
            appVersionName = "appVersionName",
            libVersionName = "libSimprintsVersionName",
            language = "en",
            device = Device(
                "30",
                "Google_Pixel 4a",
                "deviceId"
            ),
            databaseInfo = DatabaseInfo(1)
        )
        this.put("eventJson", JsonHelper.toJson(session))
        this.put("createdAt", 1611584017198)
        this.put("endedAt", endedAt)
    }

    private fun validateColumnCreation(db: SupportSQLiteDatabase, id: String) {
        val cursor = retrieveCursorWithEventById(db, id)
        Truth.assertThat(cursor.getIntWithColumnName("sessionIsClosed")).isNotNull()
    }

    private fun validateOpenSessionIsStillOpen(db: SupportSQLiteDatabase, openId: String) {
        val cursor = retrieveCursorWithEventById(db, openId)
        Truth.assertThat(cursor.getIntWithColumnName("sessionIsClosed")).isEqualTo(0)
    }

    private fun validateClosedSessionIsStillClosed(db: SupportSQLiteDatabase, closedId: String) {
        val cursor = retrieveCursorWithEventById(db, closedId)
        Truth.assertThat(cursor.getIntWithColumnName("sessionIsClosed")).isEqualTo(1)
    }

    private fun retrieveCursorWithEventById(db: SupportSQLiteDatabase, id: String): Cursor =
        db.query("SELECT * from DbEvent where id= ?", arrayOf(id)).apply { moveToNext() }

    companion object {
        private const val TEST_DB = "test"
    }

}
