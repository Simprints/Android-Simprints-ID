package com.simprints.id.data.db.event.domain.models.session

import android.os.Build
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.events.eventLabels
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent.Companion.EVENT_VERSION
import com.simprints.id.sampledata.SampleDefaults.CREATED_AT
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.id.sampledata.SampleDefaults.ENDED_AT
import com.simprints.id.sampledata.SampleDefaults.GUID1
import com.simprints.id.sampledata.SessionCaptureEventSample
import org.junit.Test

class SessionCaptureEventTest {

    @Test
    fun create_SessionCaptureEvent() {
        val appVersionNameArg = "appVersionName"
        val libSimprintsVersionNameArg = "libSimprintsVersionName"
        val languageArg = "language"
        val deviceArg = Device(
            Build.VERSION.SDK_INT.toString(),
            Build.MANUFACTURER + "_" + Build.MODEL,
            GUID1
        )

        val databaseInfoArg = DatabaseInfo(2, recordCount = 2)
        val locationArg = Location(0.0, 0.0)

        val event = SessionCaptureEventSample.getEvent(eventLabels)
        event.payload.location = locationArg
        event.payload.analyticsId = GUID1
        event.payload.endedAt = ENDED_AT

        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(eventLabels.copy())
        assertThat(event.type).isEqualTo(SESSION_CAPTURE)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(SESSION_CAPTURE)
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(appVersionName).isEqualTo(appVersionNameArg)
            assertThat(libVersionName).isEqualTo(libSimprintsVersionNameArg)
            assertThat(language).isEqualTo(languageArg)
            assertThat(device).isEqualTo(deviceArg)
            assertThat(databaseInfo).isEqualTo(databaseInfoArg)
            assertThat(analyticsId).isEqualTo(GUID1)
            assertThat(location).isEqualTo(locationArg)
        }
    }
}
