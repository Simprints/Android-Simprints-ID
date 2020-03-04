package com.simprints.id.data.db.session.remote

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.sessionEvents.createFakeClosedSession
import com.simprints.id.commontesttools.sessionEvents.createFakeOpenSession
import com.simprints.id.testtools.TestApplication
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SessionRemoteDataSourceImplTest {

    private val timeHelper: TimeHelper = TimeHelperImpl()

    @Test
    fun closedSessions_shouldBeFilteredOutToBeUploaded() {
        val sessionRemoteDataSourceSpy = buildRemoteDataSource()

        sessionRemoteDataSourceSpy.apply {
            val sessions = listOf(
                createFakeOpenSession(timeHelper),
                createFakeClosedSession(timeHelper)
            )

            val filterTask = sessions.filterClosedSessions()

            filterTask.apply {
                assertThat(size).isEqualTo(1)
                assertThat(first().isClosed()).isTrue()
            }
        }
    }

    private fun buildRemoteDataSource() = spyk(SessionRemoteDataSourceImpl(mockk(), mockk()))
}
