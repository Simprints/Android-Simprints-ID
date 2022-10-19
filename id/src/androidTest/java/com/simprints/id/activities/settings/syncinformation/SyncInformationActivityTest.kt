package com.simprints.id.activities.settings.syncinformation

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.id.R
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.images.ImageRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.testtools.unit.robolectric.createActivity
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.fakes.RoboMenuItem

@RunWith(AndroidJUnit4::class)
@Config(shadows = [ShadowAndroidXMultiDex::class])
class SyncInformationActivityTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val downSyncHelper: EventDownSyncHelper = mockk(relaxed = true)
    private val eventRepository: EventRepository = mockk(relaxed = true)
    private val enrolmentRecordManager: EnrolmentRecordManager = mockk(relaxed = true)
    private val projectId: String = DEFAULT_PROJECT_ID
    private val eventDownSyncScopeRepository: EventDownSyncScopeRepository = mockk(relaxed = true)
    private val imageRepository: ImageRepository = mockk(relaxed = true)
    private val configManager = mockk<ConfigManager>()

    @Test
    @Ignore("We are ignoring some robo tests until we finish the update to hilt")
    fun check_activity_fetches_info_only_after_resume() {

        val controller = createActivity<SyncInformationActivity>()

        coVerify(exactly = 0) { enrolmentRecordManager.count(any()) }

        controller.resume()

        coVerify(exactly = 1) { enrolmentRecordManager.count(any()) }
    }

    @Test
    @Ignore("We are ignoring some robo tests until we finish the update to hilt")
    fun check_activity_fetches_info_after_each_resume() {

        val controller = createActivity<SyncInformationActivity>()

        val times = 3
        repeat(times) {
            controller.resume()
            controller.pause()
            controller.stop()
            controller.start()
        }

        coVerify(exactly = times) { enrolmentRecordManager.count(any()) }
    }

    @Test
    @Ignore("We are ignoring some robo tests until we finish the update to hilt")
    fun check_refresh_button_fetches_info() {

        val controller = createActivity<SyncInformationActivity>()

        controller.get().onOptionsItemSelected(RoboMenuItem(R.id.sync_redo))

        coVerify(exactly = 1) { enrolmentRecordManager.count(any()) }
    }
}
