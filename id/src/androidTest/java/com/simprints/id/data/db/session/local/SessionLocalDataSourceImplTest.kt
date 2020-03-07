package com.simprints.id.data.db.session.local

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.utils.isValidGuid
import com.simprints.id.commontesttools.sessionEvents.createFakeClosedSession
import com.simprints.id.commontesttools.sessionEvents.createFakeOpenSession
import com.simprints.id.data.db.session.domain.models.SessionQuery
import com.simprints.id.data.db.session.domain.models.events.AlertScreenEvent
import com.simprints.id.data.db.session.domain.models.events.AlertScreenEvent.AlertScreenEventType.DIFFERENT_PROJECT_ID
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.data.db.session.local.SessionLocalDataSourceImpl.Companion.START_TIME
import com.simprints.id.data.db.session.local.models.DbSession
import com.simprints.id.data.db.session.local.models.toDomain
import com.simprints.id.exceptions.safe.session.SessionDataSourceException
import com.simprints.id.tools.TimeHelperImpl
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.Sort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionLocalDataSourceImplTest {

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    private val configForTest by lazy {
        RealmConfiguration
            .Builder()
            .inMemory()
            .name("sessions").build()
    }
    private val realmForTest by lazy {
        Realm.getInstance(configForTest)
    }

    @MockK lateinit var sessionLocalDataSource: SessionLocalDataSource
    @MockK lateinit var realmConfigBuilder: SessionRealmConfigBuilder

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Realm.init(ctx)
        sessionLocalDataSource = SessionLocalDataSourceImpl(ctx, mockk(relaxed = true), TimeHelperImpl(), realmConfigBuilder)
        every { realmConfigBuilder.build(any(), any()) } returns configForTest
        runBlockingInIO {
            Realm.deleteRealm(configForTest)
        }
    }

    @Test
    fun create_shouldStoreANewSession() {
        runBlockingInIO {
            assertThat(sessionLocalDataSource.count(SessionQuery())).isEqualTo(0)
            sessionLocalDataSource.create(APP_VERSION_NAME, LIB_SIMPRINTS_VERSION_NAME, LANGUAGE, DEVICE_ID)
            assertThat(sessionLocalDataSource.count(SessionQuery())).isEqualTo(1)
        }
    }

    @Test
    fun create_shouldCloseAnyOtherSession() {
        runBlockingInIO {
            saveFakeSessions(realmForTest, listOf(createFakeOpenSession(TimeHelperImpl())))

            sessionLocalDataSource.create(APP_VERSION_NAME, LIB_SIMPRINTS_VERSION_NAME, LANGUAGE, DEVICE_ID)

            val sessions = realmForTest.where(DbSession::class.java).findAll().sort(START_TIME, Sort.DESCENDING)
            assertThat(sessions.first()?.toDomain()?.isClosed()).isFalse()
            assertThat(sessions[1]?.toDomain()?.isClosed()).isTrue()
        }
    }

    @Test
    fun create_shouldHaveTheRightContent() {
        runBlockingInIO {
            sessionLocalDataSource.create(APP_VERSION_NAME, LIB_SIMPRINTS_VERSION_NAME, LANGUAGE, DEVICE_ID)

            val session = realmForTest.where(DbSession::class.java).findAll().sort(START_TIME, Sort.DESCENDING).first()
            validateNewSession(session)
        }
    }

    @Test
    fun create_shouldWrapAnyException() {
        runBlockingInIO {
            every { realmConfigBuilder.build(any(), any()) } throws Throwable("Missing config")
            checkException<SessionDataSourceException> {
                sessionLocalDataSource.create(APP_VERSION_NAME, LIB_SIMPRINTS_VERSION_NAME, LANGUAGE, DEVICE_ID)
            }
        }
    }

    @Test
    fun countByProjectIt_shouldReturnTheRightCount() {
        runBlockingInIO {
            val storedSessions = 4
            val sessions = (0 until storedSessions - 1).map { createFakeOpenSession(TimeHelperImpl()) }
            saveFakeSessions(realmForTest, sessions + createFakeOpenSession(TimeHelperImpl(), "projectId"))

            assertThat(sessionLocalDataSource.count(SessionQuery())).isEqualTo(storedSessions)
            assertThat(sessionLocalDataSource.count(SessionQuery(projectId = "projectId"))).isEqualTo(1)
        }
    }

    @Test
    fun countByOpen_shouldReturnTheRightCount() {
        runBlockingInIO {
            val storedSessions = 4
            val sessions = (0 until storedSessions - 1).map { createFakeClosedSession(TimeHelperImpl()) }
            saveFakeSessions(realmForTest, sessions + createFakeOpenSession(TimeHelperImpl(), "projectId"))

            assertThat(sessionLocalDataSource.count(SessionQuery())).isEqualTo(storedSessions)
            assertThat(sessionLocalDataSource.count(SessionQuery(openSession = true))).isEqualTo(1)
        }
    }

    @Test
    fun count_shouldWrapAnyException() {
        runBlockingInIO {
            every { realmConfigBuilder.build(any(), any()) } throws Throwable("Missing config")
            checkException<SessionDataSourceException> {
                sessionLocalDataSource.count(SessionQuery())
            }
        }
    }

    @Test
    fun loadByProjectIt_shouldReturnTheRightResults() {
        runBlockingInIO {
            val storedSessions = 4
            val sessions = (0 until storedSessions - 1).map { createFakeOpenSession(TimeHelperImpl()) }
            val specificSession = createFakeOpenSession(TimeHelperImpl(), "projectId")
            saveFakeSessions(realmForTest, sessions + specificSession)

            with(sessionLocalDataSource) {
                assertThat(load(SessionQuery()).ids()).containsExactlyElementsIn((sessions + specificSession).ids())
                assertThat(load(SessionQuery(projectId = "projectId")).ids()).isEqualTo(listOf(specificSession.id))
            }
        }
    }

    @Test
    fun loadByClose_shouldReturnTheRightResults() {
        runBlockingInIO {
            val storedSessions = 4
            val sessions = (0 until storedSessions - 1).map { createFakeOpenSession(TimeHelperImpl()) }
            val specificSession = createFakeClosedSession(TimeHelperImpl())
            saveFakeSessions(realmForTest, sessions + specificSession)

            with(sessionLocalDataSource) {
                assertThat(load(SessionQuery()).ids()).containsExactlyElementsIn((sessions + specificSession).ids())
                assertThat(load(SessionQuery(openSession = false)).ids()).isEqualTo(listOf(specificSession.id))
            }
        }
    }

    @Test
    fun load_shouldWrapAnyException() {
        runBlockingInIO {
            every { realmConfigBuilder.build(any(), any()) } throws Throwable("Missing config")
            checkException<SessionDataSourceException> {
                sessionLocalDataSource.load(SessionQuery())
            }
        }
    }

    @Test
    fun delete_shouldDeleteAllSessions() {
        runBlockingInIO {
            val storedSessions = 4
            val sessions = (0 until storedSessions).map { createFakeOpenSession(TimeHelperImpl()) }
            saveFakeSessions(realmForTest, sessions)
            assertThat(realmForTest.where(DbSession::class.java).findAll().count()).isEqualTo(storedSessions)

            sessionLocalDataSource.delete(SessionQuery())

            assertThat(realmForTest.where(DbSession::class.java).findAll().count()).isEqualTo(0)
        }
    }

    @Test
    fun deleteByClose_shouldDeleteCloseSession() {
        runBlockingInIO {
            val storedSessions = 4
            val sessions = (0 until storedSessions - 1).map { createFakeClosedSession(TimeHelperImpl()) }
            val specificSession = createFakeOpenSession(TimeHelperImpl())
            saveFakeSessions(realmForTest, sessions + specificSession)
            assertThat(realmForTest.where(DbSession::class.java).findAll().count()).isEqualTo(storedSessions)

            sessionLocalDataSource.delete(SessionQuery(openSession = false))

            assertThat(realmForTest.where(DbSession::class.java).findAll().count()).isEqualTo(1)
        }
    }

    @Test
    fun delete_shouldWrapAnyException() {
        runBlockingInIO {
            every { realmConfigBuilder.build(any(), any()) } throws Throwable("Missing config")
            checkException<SessionDataSourceException> {
                sessionLocalDataSource.delete(SessionQuery())
            }
        }
    }

    @Test
    fun update_shouldUpdateStoredSession() {
        runBlockingInIO {
            val session = createFakeOpenSession(TimeHelperImpl())
            saveFakeSessions(realmForTest, listOf(session))

            sessionLocalDataSource.update(session.id) {
                it.language = "fr"
            }

            val sessionStored = realmForTest.where(DbSession::class.java).findFirst()
            assertThat(sessionStored?.language).isEqualTo("fr")
        }
    }

    @Test
    fun update_shouldWrapAnyException() {
        runBlockingInIO {
            every { realmConfigBuilder.build(any(), any()) } throws Throwable("Missing config")
            checkException<SessionDataSourceException> {
                sessionLocalDataSource.update("some_id") {}
            }
        }
    }

    @Test
    fun updateCurrentSession_shouldUpdateOnlyCurrentSession() {
        runBlockingInIO {
            val currentSession = createFakeOpenSession(TimeHelperImpl())
            val oldSession = createFakeClosedSession(TimeHelperImpl())
            saveFakeSessions(realmForTest, listOf(currentSession, oldSession))

            sessionLocalDataSource.updateCurrentSession {
                it.language = "fr"
            }

            val currentSessionStored = realmForTest.where(DbSession::class.java)
                .equalTo("id", currentSession.id)
                .findFirst()
            assertThat(currentSessionStored?.language).isEqualTo("fr")

            val oldSessionStored = realmForTest.where(DbSession::class.java)
                .equalTo("id", oldSession.id)
                .findFirst()
            assertThat(oldSessionStored?.language).isEqualTo("en")
        }
    }

    @Test
    fun updateCurrentSession_shouldWrapAnyException() {
        runBlockingInIO {
            every { realmConfigBuilder.build(any(), any()) } throws Throwable("Missing config")
            checkException<SessionDataSourceException> {
                sessionLocalDataSource.updateCurrentSession {}
            }
        }
    }

    @Test
    fun addEventToCurrentSession_shouldUpdateOnlyCurrentSession() {
        runBlockingInIO {
            val currentSession = createFakeOpenSession(TimeHelperImpl())
            val oldSession = createFakeClosedSession(TimeHelperImpl())
            saveFakeSessions(realmForTest, listOf(currentSession, oldSession))

            sessionLocalDataSource.addEventToCurrentSession(AlertScreenEvent(0, DIFFERENT_PROJECT_ID))

            val currentSessionStored = realmForTest.where(DbSession::class.java)
                .equalTo("id", currentSession.id)
                .findFirst()
            assertThat(currentSessionStored?.realmEvents?.count()).isEqualTo(1)

            val oldSessionStored = realmForTest.where(DbSession::class.java)
                .equalTo("id", oldSession.id)
                .findFirst()
            assertThat(oldSessionStored?.realmEvents?.count()).isEqualTo(0)
        }
    }

    @Test
    fun addEventToCurrentSession_shouldWrapAnyException() {
        runBlockingInIO {
            every { realmConfigBuilder.build(any(), any()) } throws Throwable("Missing config")
            checkException<SessionDataSourceException> {
                sessionLocalDataSource.addEventToCurrentSession(AlertScreenEvent(0, DIFFERENT_PROJECT_ID))
            }
        }
    }

    private fun validateNewSession(session: DbSession?) {
        with(session!!.toDomain()) {
            assertThat(id.isValidGuid()).isTrue()
            assertThat(projectId).isEqualTo("NOT_SIGNED_IN")
            assertThat(appVersionName).isEqualTo(APP_VERSION_NAME)
            assertThat(libVersionName).isEqualTo(LIB_SIMPRINTS_VERSION_NAME)
            assertThat(language).isEqualTo(LANGUAGE)
            assertThat(device.deviceId).isEqualTo(DEVICE_ID)
            assertThat(device.androidSdkVersion).isEqualTo(Build.VERSION.SDK_INT.toString())
            assertThat(device.deviceModel).isEqualTo(Build.MANUFACTURER + "_" + Build.MODEL)
            assertThat(databaseInfo.recordCount).isNull()
            assertThat(databaseInfo.sessionCount).isEqualTo(0)
        }
    }

    private fun saveFakeSessions(realm: Realm, sessions: List<SessionEvents>) {
        every { realmConfigBuilder.build(any(), any()) } returns configForTest
        realm.executeTransaction {
            sessions.forEach {
                realm.insert(DbSession(it))
            }
        }
    }


    private inline fun <reified T : Throwable> checkException(block: () -> Unit) {
        try {
            block()
            Assert.fail("No exception thrown")
        } catch (t: Throwable) {
            if (t !is T) {
                Assert.fail("Different exception threw")
            }
        }
    }

    private fun runBlockingInIO(block: suspend () -> Unit) {
        runBlocking {
            withContext(Dispatchers.IO) {
                block()
            }
        }
    }

    private suspend fun Flow<SessionEvents>.ids() = this.toList().map { it.id }
    private fun List<SessionEvents>.ids() = this.map { it.id }

    companion object {
        private const val APP_VERSION_NAME = "APP_VERSION_NAME"
        private const val LIB_SIMPRINTS_VERSION_NAME = "LIB_SIMPRINTS_VERSION_NAME"
        private const val LANGUAGE = "en"
        private const val DEVICE_ID = "DEVICE_ID"

    }
}
