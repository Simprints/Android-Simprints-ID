package com.simprints.face.capture.models

import android.content.res.Configuration
import android.content.res.Resources
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class ScreenOrientationTest {
    @MockK
    lateinit var resources: Resources

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun `when the resources are provided, then the correct screen orientation is mapped`() {
        val orientationMap = mapOf(
            Configuration.ORIENTATION_LANDSCAPE to ScreenOrientation.Landscape,
            Configuration.ORIENTATION_PORTRAIT to ScreenOrientation.Portrait,
        )
        orientationMap.forEach { entry ->
            val resourceOrientation = entry.key
            val expectedOrientation = entry.value

            every { resources.configuration } returns mockk {
                orientation = resourceOrientation
            }
            val orientation = ScreenOrientation.getCurrentOrientation(resources)
            assertThat(orientation).isEqualTo(expectedOrientation)
        }
    }
}
