package com.simprints.id.data.db.event.domain.models.session

import android.os.Build
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent.SessionCapturePayload
import com.simprints.id.orchestrator.SOME_GUID1
import org.junit.Test

class SessionCaptureEventTest {

    @Test
    fun create_SessionCaptureEvent() {
        val appVersionName = "appVersionName"
        val libSimprintsVersionName = "libSimprintsVersionName"
        val language = "language"
        val device = Device(
            Build.VERSION.SDK_INT.toString(),
            Build.MANUFACTURER + "_" + Build.MODEL,
            SOME_GUID1)

        val databaseInfo = DatabaseInfo(2)

        val event = SessionCaptureEvent(
            1,
            SOME_GUID1,
            DEFAULT_PROJECT_ID,
            appVersionName,
            libSimprintsVersionName,
            language,
            device,
            databaseInfo)

        assertThat(event.id).isNotNull()
        assertThat(event.labels).containsExactly(SessionIdLabel(SOME_GUID1))
        assertThat(event.type).isEqualTo(SESSION_CAPTURE)
        with(event.payload as SessionCapturePayload) {
            assertThat(createdAt).isEqualTo(1)
            assertThat(endedAt).isEqualTo(0)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(SESSION_CAPTURE)
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(this.appVersionName).isEqualTo(appVersionName)
            assertThat(this.libVersionName).isEqualTo(libSimprintsVersionName)
            assertThat(this.language).isEqualTo(language)
            assertThat(this.device).isEqualTo(device)
            assertThat(this.databaseInfo).isEqualTo(databaseInfo)
        }

    }
}
