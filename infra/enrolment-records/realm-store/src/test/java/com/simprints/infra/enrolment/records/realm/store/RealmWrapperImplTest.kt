package com.simprints.infra.enrolment.records.realm.store

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.enrolment.records.realm.store.config.RealmConfig
import com.simprints.infra.enrolment.records.realm.store.exceptions.RealmUninitialisedException
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.keyprovider.LocalDbKey
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RealmWrapperImplTest {
    companion object {
        private const val PROJECT_ID = "projectId"
    }

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var authStore: AuthStore

    @MockK
    private lateinit var secureLocalDbKeyProviderMock: SecurityManager

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var realmConfig: RealmConfig

    @MockK
    private lateinit var configuration: RealmConfiguration

    @MockK
    private lateinit var realm: Realm

    private lateinit var realmWrapper: RealmWrapperImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { authStore.signedInProjectId } returns PROJECT_ID
        every {
            secureLocalDbKeyProviderMock.getLocalDbKeyOrThrow(PROJECT_ID)
        } returns LocalDbKey("DatabaseName", "DatabaseKey".toByteArray())
        justRun { secureLocalDbKeyProviderMock.recreateLocalDatabaseKey(PROJECT_ID) }

        every { realmConfig.get(any(), any()) } returns configuration
        every { configuration.path } returns "path"

        mockkObject(Realm.Companion)
        every { Realm.Companion.open(any()) } returns realm
        coJustRun { realm.write(any()) }

        realmWrapper = RealmWrapperImpl(
            context,
            realmConfig,
            secureLocalDbKeyProviderMock,
            authStore,
            testCoroutineRule.testCoroutineDispatcher,
        )
    }

    @After
    fun tearDown() {
        unmockkObject(Realm.Companion)
    }

    @Test
    fun `test useRealmInstance creates realm instance should throw if no signed in project`() = runTest {
        assertThrows<RealmUninitialisedException> {
            every { authStore.signedInProjectId } returns ""
            realmWrapper = RealmWrapperImpl(
                ApplicationProvider.getApplicationContext(),
                realmConfig,
                secureLocalDbKeyProviderMock,
                authStore,
                testCoroutineRule.testCoroutineDispatcher,
            )
            realmWrapper.readRealm { }
        }
    }

    @Test
    fun `test recreate db if it is corrupted`() = runTest {
        // Given
        every {
            Realm.open(
                configuration,
            )
        } throws IllegalStateException("[RLM_ERR_INVALID_DATABASE]: Failed to open Realm file at path") andThen
            mockk<Realm>(relaxed = true)
        every { Realm.deleteRealm(configuration) } just Runs

        // When
        realmWrapper.writeRealm { }
        // Then
        verify {
            Realm.deleteRealm(configuration)
            secureLocalDbKeyProviderMock.recreateLocalDatabaseKey(PROJECT_ID)
            context.startService(any()) // Start ResetDownSyncService service
        }
        verify(exactly = 2) { Realm.open(configuration) }
    }
}
