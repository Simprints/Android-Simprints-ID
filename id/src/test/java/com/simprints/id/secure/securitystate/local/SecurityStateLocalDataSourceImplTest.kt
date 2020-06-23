package com.simprints.id.secure.securitystate.local

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.secure.models.SecurityState
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SecurityStateLocalDataSourceImplTest {

    @MockK lateinit var mockSettingsPreferencesManager: SettingsPreferencesManager

    private lateinit var localDataSource: SecurityStateLocalDataSourceImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        localDataSource = SecurityStateLocalDataSourceImpl(mockSettingsPreferencesManager)
    }

    @Test
    fun shouldGetSecurityStatusFromSharedPreferences() {
        val expected = SecurityState.Status.COMPROMISED
        every {
            mockSettingsPreferencesManager.securityStatus
        } returns expected

        val actual = localDataSource.getSecurityStatus()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun shouldSetSecurityStatusInSharedPreferences() {
        val status = SecurityState.Status.PROJECT_ENDED

        localDataSource.setSecurityStatus(status)

        verify { mockSettingsPreferencesManager.securityStatus = status }
    }

}
