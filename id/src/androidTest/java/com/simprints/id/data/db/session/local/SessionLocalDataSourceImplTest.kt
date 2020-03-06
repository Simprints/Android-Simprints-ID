package com.simprints.id.data.db.session.local

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.session.domain.models.SessionQuery
import com.simprints.id.tools.TimeHelperImpl
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.realm.RealmConfiguration
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class SessionLocalDataSourceImplTest {

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    @MockK lateinit var sessionLocalDataSource: SessionLocalDataSource
    @MockK lateinit var realmConfigBuilder: SessionRealmConfigBuilder

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        every { realmConfigBuilder.build(any(), any()) } returns RealmConfiguration.Builder().inMemory().name("sessions").build()

        sessionLocalDataSource = SessionLocalDataSourceImpl(ctx, mockk(relaxed = true), TimeHelperImpl(), realmConfigBuilder)
    }

    @Test
    fun create_shouldStoreANewSession() {
        runBlocking {
            assertThat(sessionLocalDataSource.count(SessionQuery())).isEqualTo(0)
            sessionLocalDataSource.create(APP_VERSION_NAME, LIB_SIMPRINTS_VERSION_NAME, LANGUAGE, DEVICE_ID)
            assertThat(sessionLocalDataSource.count(SessionQuery())).isEqualTo(1)
        }
    }

    companion object {
        private const val APP_VERSION_NAME = "APP_VERSION_NAME"
        private const val LIB_SIMPRINTS_VERSION_NAME = "LIB_SIMPRINTS_VERSION_NAME"
        private const val LANGUAGE = "en"
        private const val DEVICE_ID = "DEVICE_ID"

    }
}
