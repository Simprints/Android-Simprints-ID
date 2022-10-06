package com.simprints.id.activities.settings.syncinformation

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.testtools.di.TestAppModule
import com.simprints.id.testtools.di.TestDataModule
import com.simprints.id.testtools.di.TestViewModelModule
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.images.ImageRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.testtools.unit.robolectric.createActivity
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.fakes.RoboMenuItem

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SyncInformationActivityTest {

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val appModule by lazy {
        TestAppModule(app)
    }

    private val dataModule by lazy {
        TestDataModule()
    }

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val downSyncHelper: EventDownSyncHelper = mockk(relaxed = true)
    private val eventRepository: EventRepository = mockk(relaxed = true)
    private val subjectRepository: SubjectRepository = mockk(relaxed = true)
    private val projectId: String = DEFAULT_PROJECT_ID
    private val eventDownSyncScopeRepository: EventDownSyncScopeRepository = mockk(relaxed = true)
    private val imageRepository: ImageRepository = mockk(relaxed = true)
    private val configManager = mockk<ConfigManager>()
    private val viewModelModule by lazy {
        TestViewModelModule(
            syncInformationViewModelFactorRule = DependencyRule.ReplaceRule {
                SyncInformationViewModelFactory(
                    downSyncHelper,
                    eventRepository,
                    subjectRepository,
                    projectId,
                    eventDownSyncScopeRepository,
                    imageRepository,
                    configManager,
                    testCoroutineRule.testCoroutineDispatcher,
                )
            }
        )
    }

    @Before
    fun setUp() {
        UnitTestConfig(
            appModule,
            dataModule = dataModule,
            viewModelModule = viewModelModule,
        ).fullSetup().inject(this)
    }

    @Test
    @Ignore("We are ignoring some robo tests until we finish the update to hilt")
    fun `check activity fetches info only after resume`() {

        val controller = createActivity<SyncInformationActivity>()

        coVerify(exactly = 0) { subjectRepository.count(any()) }

        controller.resume()

        coVerify(exactly = 1) { subjectRepository.count(any()) }
    }

    @Test
    @Ignore("We are ignoring some robo tests until we finish the update to hilt")
    fun `check activity fetches info after each resume`() {

        val controller = createActivity<SyncInformationActivity>()

        val times = 3
        repeat(times) {
            controller.resume()
            controller.pause()
            controller.stop()
            controller.start()
        }

        coVerify(exactly = times) { subjectRepository.count(any()) }
    }

    @Test
    @Ignore("We are ignoring some robo tests until we finish the update to hilt")
    fun `check refresh button fetches info`() {

        val controller = createActivity<SyncInformationActivity>()

        controller.get().onOptionsItemSelected(RoboMenuItem(R.id.sync_redo))

        coVerify(exactly = 1) { subjectRepository.count(any()) }
    }
}
