package com.simprints.id.activities

import com.nhaarman.mockito_kotlin.doReturn
import com.simprints.id.Application
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.FirebaseAnalyticsManager
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.testUtils.roboletric.createMockForDbManager
import com.simprints.id.testUtils.roboletric.createMockForLocalDbManager
import com.simprints.id.testUtils.roboletric.createRoboAboutActivity
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.android.synthetic.main.activity_about.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.spy
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import com.simprints.id.shared.anyNotNull
import com.simprints.id.shared.whenever

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class AboutActivityTest : RxJavaTest() {

    private lateinit var analyticsManagerMock: AnalyticsManager
    private lateinit var app: Application

    @Before
    fun setUp() {
        app = (RuntimeEnvironment.application as Application)
        createMockForLocalDbManager(app)
        whenever(app.dbManager.getPeopleCount(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull())).thenReturn(Single.just(0))

        createMockForDbManager(app)
        mockAnalyticsManager()
    }

    private fun mockAnalyticsManager() {
        analyticsManagerMock = Mockito.mock(FirebaseAnalyticsManager::class.java)
        app.analyticsManager = analyticsManagerMock
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
        val dbManagerMock = spy(app.dbManager)
        doReturn(Completable.create { }).`when`(dbManagerMock).recoverLocalDb(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull())
        app.dbManager = dbManagerMock

        val controller = createRoboAboutActivity().start().resume().visible()
        val activity = controller.get()

        assertTrue(activity.bt_recoverDb.isEnabled)

        activity.bt_recoverDb.performClick()

        assertFalse(activity.bt_recoverDb.isEnabled)
    }
}
