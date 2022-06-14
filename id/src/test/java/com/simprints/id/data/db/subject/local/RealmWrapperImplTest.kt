package com.simprints.id.data.db.subject.local

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.simprints.id.data.db.subject.migration.SubjectsRealmConfig
import com.simprints.id.exceptions.unexpected.RealmUninitialisedException
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.coroutines.TestDispatcherProvider
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.*
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.internal.RealmCore
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config


@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class RealmWrapperImplTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    private val testDispatcherProvider = TestDispatcherProvider(testCoroutineRule)

    private lateinit var realmWrapper: RealmWrapperImpl

    @Before
    fun setUp() {
        mockkStatic(Realm::class.java.name)
        mockkStatic(RealmCore::class.java.name)

        every { Realm.init(any()) } just Runs
        every { Realm.getDefaultModule() } returns null
        every { RealmCore.loadLibrary(any()) } just Runs
        mockkStatic(RealmConfiguration::class.java.name)
        every { Realm.getInstance(any()) } returns mockk()

        mockkObject(SubjectsRealmConfig)
        every { SubjectsRealmConfig.get(any(), any(), any()) } returns mockk()


        realmWrapper = RealmWrapperImpl(
            ApplicationProvider.getApplicationContext(),
            mockk(),
            testDispatcherProvider
        )
    }

    @Test
    fun `test useRealmInstance creates realm instance and returns correct values`() = runBlocking {

        val anyNumber = realmWrapper.useRealmInstance { 10 }
        verify { Realm.getInstance(any()) }
        Truth.assertThat(anyNumber).isEqualTo(10)
    }

    @Test(expected = RealmUninitialisedException::class)
    fun `test useRealmInstance creates realm instance should throw if localdbkey is null`() =
        runBlocking {
            realmWrapper = RealmWrapperImpl(
                ApplicationProvider.getApplicationContext(),
                null,
                testDispatcherProvider
            )

            val anyNumber = realmWrapper.useRealmInstance { 10 }
            // Then should throw RealmUninitialisedException
        }
}
