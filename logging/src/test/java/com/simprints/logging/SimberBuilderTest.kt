package com.simprints.logging

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.simprints.logging.LoggingTestUtils.setDebugBuildConfig
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
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
        setDebugBuildConfig(true)

        SimberBuilder.initialize(context)

        assert(Timber.treeCount == 1)
    }

    @Test
    fun `initialize in debug mode should not create performance monitor`() {
        setDebugBuildConfig(true)

        val trace = SimberBuilder.getTrace("Test Name", Simber)

        assert(trace.newTrace == null)
    }

    @Test
    fun `initialize in release mode should create 2 trees`() {
        setDebugBuildConfig(false)

        try {
            SimberBuilder.initialize(context)
        } catch (ex: Exception) {
            assert(ex is IllegalStateException)
        }
    }


}
