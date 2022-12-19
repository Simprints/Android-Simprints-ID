package com.simprints.infra.realm

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.login.LoginManager
import com.simprints.infra.realm.config.RealmConfig
import com.simprints.infra.realm.exceptions.RealmUninitialisedException
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.keyprovider.LocalDbKey
import io.mockk.*
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.exceptions.RealmFileException
import io.realm.internal.RealmCore
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RealmWrapperImplTest {

    companion object {
        private const val PROJECT_ID = "projectId"
    }

    private val loginManagerMock = mockk<LoginManager> {
        every { getSignedInProjectIdOrEmpty() } returns PROJECT_ID
    }

    private val secureLocalDbKeyProviderMock = mockk<SecurityManager> {
        every { getLocalDbKeyOrThrow(PROJECT_ID) } returns LocalDbKey(
            "DatabaseName",
            "DatabaseKey".toByteArray()
        )
        justRun { recreateLocalDatabaseKey(PROJECT_ID) }
    }
    private val context:Context = mockk()
    private lateinit var realmWrapper: RealmWrapperImpl
    private var configuration: RealmConfiguration = mockk {
        every { path } returns "path"
    }

    @Before
    fun setUp() {
        mockkStatic(Realm::class.java.name)
        mockkStatic(RealmCore::class.java.name)

        every { Realm.init(any()) } just Runs
        every { Realm.getDefaultModule() } returns null
        every { RealmCore.loadLibrary(any()) } just Runs
        mockkStatic(RealmConfiguration::class.java.name)
        every { Realm.getInstance(any()) } returns mockk(relaxed = true)

        mockkObject(RealmConfig)
        every { RealmConfig.get(any(), any(), any()) } returns configuration



        realmWrapper = RealmWrapperImpl(
            context,
            secureLocalDbKeyProviderMock,
            loginManagerMock,
        )
    }

    @Test
    fun `test useRealmInstance creates realm instance and returns correct values`() =
        runTest {

            val anyNumber = realmWrapper.useRealmInstance { 10 }
            verify { Realm.getInstance(any()) }
            assertThat(anyNumber).isEqualTo(10)
        }

    @Test(expected = RealmUninitialisedException::class)
    fun `test useRealmInstance creates realm instance should throw if no signed in project is null`() =
        runTest {
            every { loginManagerMock.getSignedInProjectIdOrEmpty() } returns ""
            realmWrapper = RealmWrapperImpl(
                ApplicationProvider.getApplicationContext(),
                secureLocalDbKeyProviderMock,
                loginManagerMock,
            )
            realmWrapper.useRealmInstance { }
            // Then should throw RealmUninitialisedException
        }

    @Test
    fun `test recreate db if it is corrupted`() {
        runTest {
            //Given
            justRun { secureLocalDbKeyProviderMock.recreateLocalDatabaseKey(PROJECT_ID) }
            every { Realm.getInstance(configuration) } throws RealmFileException(
                RealmFileException.Kind.NOT_FOUND,
                "file not found"
            ) andThen mockk<Realm>(relaxed = true)
            every { Realm.deleteRealm(configuration) } returns true

            //When
            realmWrapper.useRealmInstance { }
            //Then
            verify {
                Realm.deleteRealm(configuration)
                secureLocalDbKeyProviderMock.recreateLocalDatabaseKey(PROJECT_ID)
                context.startService(any()) // Start ResetDownSyncService service
            }
            verify(exactly = 2) { Realm.getInstance(configuration) }
        }

    }
}
