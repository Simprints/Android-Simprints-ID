package com.simprints.infra.projectsecuritystore.securitystate.repo.local

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.projectsecuritystore.securitystate.models.SecurityState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class SecurityStateLocalDataSourceTest {

    private val editor = mockk<SharedPreferences.Editor>(relaxed = true)
    private val prefs = mockk<SharedPreferences>(relaxed = true) {
        every { edit() } returns editor
    }
    private val ctx = mockk<Context> {
        every { getSharedPreferences(any(), any()) } returns prefs
    }

    private val localDataSource = SecurityStateLocalDataSource(ctx)

    @Test
    fun shouldGetSecurityStatusFromSharedPreferences() {
        val expected = SecurityState.Status.COMPROMISED
        every {
            prefs.getString(
                SecurityStateLocalDataSource.KEY_SECURITY_STATUS,
                SecurityStateLocalDataSource.DEFAULT_SECURITY_STATUS
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
            editor.putString(
                SecurityStateLocalDataSource.KEY_SECURITY_STATUS,
                status.name
            )
        }
    }
}
