package com.simprints.id.activities

import com.google.firebase.FirebaseApp
import com.nhaarman.mockito_kotlin.doReturn
import com.simprints.id.data.db.DbManager
import com.simprints.id.di.AppModuleForTests
import com.simprints.id.di.DaggerForTests
import com.simprints.id.shared.DependencyRule.*
import com.simprints.id.shared.anyNotNull
import com.simprints.id.shared.whenever
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.testUtils.roboletric.createRoboAboutActivity
import com.simprints.id.tools.delegates.lazyVar
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.android.synthetic.main.activity_debug.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class AboutActivityTest : RxJavaTest, DaggerForTests() {

    @Inject
    lateinit var dbManagerMock: DbManager

    override var module by lazyVar {
        AppModuleForTests(app,
            dbManagerRule = MockRule,
            dataManagerRule = SpyRule,
            localDbManagerRule = MockRule)
    }

    @Before
    override fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        app = (RuntimeEnvironment.application as TestApplication)
        super.setUp()
        testAppComponent.inject(this)
        dbManagerMock.initialiseDb()

        whenever(dbManagerMock.getPeopleCountFromLocalForSyncGroup(anyNotNull())).thenReturn(Single.just(0))
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
        doReturn(Completable.create { }).`when`(dbManagerMock).recoverLocalDb(anyNotNull())

        val controller = createRoboAboutActivity().start().resume().visible()
        val activity = controller.get()

        assertTrue(activity.bt_recoverDb.isEnabled)

        activity.bt_recoverDb.performClick()

        assertFalse(activity.bt_recoverDb.isEnabled)
    }
}
