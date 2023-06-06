package com.simprints.feature.fetchsubject.screen


import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FetchSubjectAlertsTest {

    @Test
    fun `should not show exit form when returning from NotFoundOnline alert`() {
        val config = FetchSubjectAlerts.subjectNotFoundOnline()
        val showExitForm = FetchSubjectAlerts.shouldShowExitForm(config.payload)
        assertThat(showExitForm).isFalse()
    }

    @Test
    fun `should show exit form when returning from NotFoundOffline alert`() {
        val config = FetchSubjectAlerts.subjectNotFoundOffline()
        val showExitForm = FetchSubjectAlerts.shouldShowExitForm(config.payload)
        assertThat(showExitForm).isTrue()
    }
}
