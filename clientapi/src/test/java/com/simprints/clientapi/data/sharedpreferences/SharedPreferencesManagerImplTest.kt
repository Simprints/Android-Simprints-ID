package com.simprints.clientapi.data.sharedpreferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManagerImpl.Companion.SESSION_ID
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManagerImpl.Companion.SHARED_PREF_KEY
import com.simprints.id.data.prefs.IdPreferencesManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test

class SharedPreferencesManagerImplTest {

    @Test
    fun `sync settings should call the core preferences manager`() {

        val idPreferenceSpy = spyk<IdPreferencesManager>()

        val preferencesManagerImpl =
            SharedPreferencesManagerImpl(mockk(relaxed = true), idPreferenceSpy)

        @Suppress("UNUSED_VARIABLE")
        val settings = preferencesManagerImpl.simprintsSyncSetting

        verify(exactly = 1) { idPreferenceSpy.simprintsSyncSetting }
    }

    @Test
    fun `co sync settings should call the core preferences manager`() {

        val idPreferenceSpy = spyk<IdPreferencesManager>()

        val preferencesManagerImpl =
            SharedPreferencesManagerImpl(mockk(relaxed = true), idPreferenceSpy)

        @Suppress("UNUSED_VARIABLE")
        val settings = preferencesManagerImpl.cosyncSyncSettings

        verify(exactly = 1) { idPreferenceSpy.cosyncSyncSetting }
    }

    @Test
    fun `modalities settings should call the core preferences manager`() {

        val idPreferenceSpy = spyk<IdPreferencesManager>()

        val preferencesManagerImpl =
            SharedPreferencesManagerImpl(mockk(relaxed = true), idPreferenceSpy)

        @Suppress("UNUSED_VARIABLE")
        val settings = preferencesManagerImpl.modalities

        verify(exactly = 1) { idPreferenceSpy.modalities }
    }

    @Test
    fun `stash session ID should put session ID in shared prefs`() {

        val idPreferenceSpy = spyk<IdPreferencesManager>()
        val sharedPrefSpy = mockk<SharedPreferences>()

        val contextMock = mockk<Context>(relaxed = true) {
            every { getSharedPreferences(any(), Context.MODE_PRIVATE) } returns sharedPrefSpy
        }


        val preferencesManagerImpl =
            SharedPreferencesManagerImpl(contextMock, idPreferenceSpy)

        val sessionID = "session_id"

        preferencesManagerImpl.stashSessionId(sessionID)

        verify(exactly = 1) { sharedPrefSpy.edit { putString(SESSION_ID, sessionID) } }
    }

    @Test
    fun `peek session ID should return session ID from shared prefs`() {

        val idPreferenceSpy = spyk<IdPreferencesManager>()
        val sharedPrefSpy = mockk<SharedPreferences>()

        val contextMock = mockk<Context>(relaxed = true) {
            every { getSharedPreferences(any(), Context.MODE_PRIVATE) } returns sharedPrefSpy
        }


        val preferencesManagerImpl =
            SharedPreferencesManagerImpl(contextMock, idPreferenceSpy)

        preferencesManagerImpl.peekSessionId()

        verify(exactly = 1) { sharedPrefSpy.getString(SESSION_ID, "") }
    }

    @Test
    fun `pop session ID should pop session ID in shared prefs`() {

        val idPreferenceSpy = spyk<IdPreferencesManager>()
        val sharedPrefSpy = mockk<SharedPreferences>()

        val contextMock = mockk<Context>(relaxed = true) {
            every { getSharedPreferences(any(), Context.MODE_PRIVATE) } returns sharedPrefSpy
        }


        val preferencesManagerImpl =
            SharedPreferencesManagerImpl(contextMock, idPreferenceSpy)
        
        preferencesManagerImpl.popSessionId()

        verify(exactly = 1) { preferencesManagerImpl.peekSessionId() }
        verify(exactly = 1) { preferencesManagerImpl.stashSessionId("") }
    }

    @Test
    fun `shared prefs should be private`() {

        val idPreferenceSpy = spyk<IdPreferencesManager>()

        val contextMock = mockk<Context>(relaxed = true)

        SharedPreferencesManagerImpl(contextMock, idPreferenceSpy)

        verify(exactly = 1) {
            contextMock.getSharedPreferences(
                SHARED_PREF_KEY,
                Context.MODE_PRIVATE
            )
        }
    }

}
