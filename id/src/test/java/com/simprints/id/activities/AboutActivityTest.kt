package com.simprints.id.activities

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.simprints.id.Application
import com.simprints.id.data.DataManager
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.di.AppComponent
import com.simprints.id.di.AppModule
import com.simprints.id.shared.anyNotNull
import com.simprints.id.shared.whenever
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.testUtils.roboletric.createMockForDbManager
import com.simprints.id.testUtils.roboletric.createMockForLocalDbManager
import com.simprints.id.testUtils.roboletric.createRoboAboutActivity
import io.reactivex.Completable
import io.reactivex.Single
import it.cosenonjaviste.daggermock.DaggerMock
import kotlinx.android.synthetic.main.activity_about.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Spy
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class AboutActivityTest : RxJavaTest() {

    @get:Rule
    val rule = DaggerMock.rule<AppComponent>(AppModule(RuntimeEnvironment.application as Application)) {
        set { Application.component = it }
    }

    @Mock val analyticsManagerMock: AnalyticsManager = mock()
    @Mock val localDbManager: AnalyticsManager = mock()
    @Spy val dataManager: DataManager = spy()

    private lateinit var app: Application

    @Before
    fun setUp() {
        app = (RuntimeEnvironment.application as Application)
        createMockForLocalDbManager(app)
        whenever(app.dbManager.getPeopleCount(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull())).thenReturn(Single.just(0))

        createMockForDbManager(app)
    }

    @Test
    fun versionTextViews_shouldBePopulated() {
        val controller = createRoboAboutActivity().start().resume().visible()
        val activity = controller.get()

        assertTrue(activity.tv_appVersion.text.isNotEmpty())
        assertTrue(activity.tv_libsimprintsVersion.text.isNotEmpty())
        assertTrue(activity.tv_scannerVersion.text.isNotEmpty())
    }

    @Test
    fun countTextViews_shouldBePopulated() {
        val controller = createRoboAboutActivity().start().resume().visible()
        val activity = controller.get()

        assertEquals("0", activity.tv_userDbCount.text)
        assertEquals("0", activity.tv_moduleDbCount.text)
        assertEquals("0", activity.tv_globalDbCount.text)
    }

    @Test
    fun recoverDbButton_disablesOncePressed() {
        doReturn(Completable.create { }).`when`(dataManager).recoverRealmDb(anyNotNull())

        val controller = createRoboAboutActivity().start().resume().visible()
        val activity = controller.get()

        assertTrue(activity.bt_recoverDb.isEnabled)

        activity.bt_recoverDb.performClick()

        assertFalse(activity.bt_recoverDb.isEnabled)
    }
}
