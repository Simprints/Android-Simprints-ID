package com.simprints.logging

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.simprints.logging.trees.PerformanceMonitoringTree
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers
import timber.log.Timber

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SimberBuilderTest {

    var context: Context = InstrumentationRegistry.getInstrumentation().context

    @Before
    fun setup() {
        FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Test
    fun `initialize in debug mode should only create debug tree`() {
        ReflectionHelpers.setStaticField(BuildConfig::class.java, "DEBUG", true)

        SimberBuilder.initialize(context)

        assert(Timber.treeCount == 1)
    }

    @Test
    fun `initialize in debug mode should not create performance monitor`() {
        ReflectionHelpers.setStaticField(BuildConfig::class.java, "DEBUG", true)

        SimberBuilder.initialize(context)

        assert(PerformanceMonitoringTree.performanceMonitor == null)
    }

    @Test
    fun `initialize in release mode should create 2 trees`() {
        ReflectionHelpers.setStaticField(BuildConfig::class.java, "DEBUG", false)

        try {
            SimberBuilder.initialize(context)
        } catch (ex: Exception) {
            assert(ex is IllegalStateException)
        }
    }


}
