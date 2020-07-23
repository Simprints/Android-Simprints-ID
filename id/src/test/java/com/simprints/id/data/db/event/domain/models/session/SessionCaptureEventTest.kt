package com.simprints.id.data.db.event.domain.models.session

import android.os.Build
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.data.db.event.domain.models.CREATED_AT
import com.simprints.id.data.db.event.domain.models.ENDED_AT
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent.Companion.EVENT_VERSION
import com.simprints.id.orchestrator.SOME_GUID1
import org.junit.Test

class SessionCaptureEventTest {

    @Test
    fun create_SessionCaptureEvent() {
        val labelsArg = EventLabels(sessionId = SOME_GUID1)
        val appVersionNameArg = "appVersionName"
        val libSimprintsVersionNameArg = "libSimprintsVersionName"
        val languageArg = "language"
        val deviceArg = Device(
            Build.VERSION.SDK_INT.toString(),
            Build.MANUFACTURER + "_" + Build.MODEL,
            SOME_GUID1)

        val databaseInfoArg = DatabaseInfo(2)
        val locationArg = Location(0.0, 0.0)

        val event = SessionCaptureEvent(
            CREATED_AT,
            SOME_GUID1,
            DEFAULT_PROJECT_ID,
            appVersionNameArg,
            libSimprintsVersionNameArg,
            languageArg,
            deviceArg,
            databaseInfoArg,
            ENDED_AT,
            ENDED_AT,
            locationArg,
            SOME_GUID1,
            labelsArg
        )

        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labelsArg)
        assertThat(event.type).isEqualTo(SESSION_CAPTURE)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(ENDED_AT)
            assertThat(uploadTime).isEqualTo(ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(SESSION_CAPTURE)
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(appVersionName).isEqualTo(appVersionNameArg)
            assertThat(libVersionName).isEqualTo(libSimprintsVersionNameArg)
            assertThat(language).isEqualTo(languageArg)
            assertThat(device).isEqualTo(deviceArg)
            assertThat(databaseInfo).isEqualTo(databaseInfoArg)
            assertThat(analyticsId).isEqualTo(SOME_GUID1)
            assertThat(location).isEqualTo(locationArg)
        }
    }
}
