package com.simprints.id.secure.securitystate.local

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.secure.models.SecurityState
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SecurityStateLocalDataSourceImplTest {

    @MockK lateinit var mockPrefs: ImprovedSharedPreferences

    private lateinit var localDataSource: SecurityStateLocalDataSourceImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        localDataSource = SecurityStateLocalDataSourceImpl(mockPrefs)
    }

    @Test
    fun shouldGetSecurityStatusFromSharedPreferences() {
        val expected = SecurityState.Status.COMPROMISED
        every {
            mockPrefs.getString(
                SecurityStateLocalDataSourceImpl.KEY_SECURITY_STATUS,
                SecurityStateLocalDataSourceImpl.DEFAULT_SECURITY_STATUS
            )
        } returns expected.name

        val actual = localDataSource.securityStatus

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun shouldSetSecurityStatusInSharedPreferences() {
        val status = SecurityState.Status.PROJECT_ENDED

        localDataSource.securityStatus = status

        verify {
            mockPrefs.edit().putPrimitive(
                SecurityStateLocalDataSourceImpl.KEY_SECURITY_STATUS,
                status.name
            )
        }
    }

}
