package com.simprints.infra.events.event.domain.models.session

import android.os.Build
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.events.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.infra.events.event.domain.models.session.SessionCaptureEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
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

        val event = SessionCaptureEvent(
            GUID1,
            DEFAULT_PROJECT_ID,
            CREATED_AT,
            listOf(
                GeneralConfiguration.Modality.FINGERPRINT,
                GeneralConfiguration.Modality.FACE
            ),
            appVersionNameArg,
            libSimprintsVersionNameArg,
            languageArg,
            deviceArg,
            databaseInfoArg,
            locationArg,
        )

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(SESSION_CAPTURE)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(SESSION_CAPTURE)
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(appVersionName).isEqualTo(appVersionNameArg)
            assertThat(libVersionName).isEqualTo(libSimprintsVersionNameArg)
            assertThat(language).isEqualTo(languageArg)
            assertThat(device).isEqualTo(deviceArg)
            assertThat(databaseInfo).isEqualTo(databaseInfoArg)
            assertThat(location).isEqualTo(locationArg)
        }
    }

}
