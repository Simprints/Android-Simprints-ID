package com.simprints.feature.fetchsubject.screen


import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FetchSubjectAlertsTest {

    @Test
    fun `when returning from NotFoundOnline alert should set wasOnline flag to true`() {
        val config = FetchSubjectAlerts.subjectNotFoundOnline()
        val wasOnline = FetchSubjectAlerts.wasOnline(config.payload)
        assertThat(wasOnline).isTrue()
    }

    @Test
    fun `when returning from NotFoundOffline alert should set wasOnline flag to false`() {
        val config = FetchSubjectAlerts.subjectNotFoundOffline()
        val wasOnline = FetchSubjectAlerts.wasOnline(config.payload)
        assertThat(wasOnline).isFalse()
    }
}
