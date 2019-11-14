package com.simprints.id.orchestrator.cache

import android.content.Context.MODE_PRIVATE
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.Application
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.id.orchestrator.steps.Step
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest

@RunWith(AndroidJUnit4::class)
class HotCacheImplTest: AutoCloseKoinTest() {

    private val hotCache by lazy {
        val context =  ApplicationProvider.getApplicationContext<Application>()
        val stepEncoder = StepEncoderImpl()
        HotCacheImpl(context.getSharedPreferences("shared", MODE_PRIVATE), stepEncoder)
    }

    @Test
    fun shouldCacheStep() {
        val step = mockStep()

        hotCache.save(step)
        val cachedSteps = hotCache.load()

        val isStepCached = cachedSteps.contains(step)
        val cachedStepCount = cachedSteps.size

        assertThat(cachedStepCount).isEqualTo(1)
        assertThat(isStepCached).isEqualTo(true)
    }

    @Test
    fun withDuplicatedSteps_shouldCacheOnlyMostRecent() {
        val oldStep = mockStep()
        val newStep = oldStep.copy(requestCode = 321)

        with(hotCache) {
            save(oldStep)
            save(newStep)
        }

        val cachedSteps = hotCache.load()
        val cachedStepCount = cachedSteps.size
        val isOldStepCached = cachedSteps.contains(oldStep)

        assertThat(cachedStepCount).isEqualTo(1)
        assertThat(cachedSteps.first()).isEqualTo(newStep)
        assertThat(isOldStepCached).isEqualTo(false)
    }

    @Test
    fun shouldCacheDifferentSteps() {
        val step1 = mockStep()
        val step2 = mockStep()

        with(hotCache) {
            save(step1)
            save(step2)
        }

        val cachedSteps = hotCache.load()
        val cachedStepCount = cachedSteps.size

        assertThat(cachedStepCount).isEqualTo(2)
        assertThat(cachedSteps).contains(step1)
        assertThat(cachedSteps).contains(step2)
    }

    @Test
    fun shouldClearCache() {
        val step = mockStep()

        with(hotCache) {
            save(step)
            clear()
        }

        val cachedSteps = hotCache.load()
        val cachedStepCount = cachedSteps.size
        val isStepCached = cachedSteps.contains(step)

        assertThat(cachedStepCount).isEqualTo(0)
        assertThat(isStepCached).isFalse()
    }

    private fun mockStep() = Step(
        requestCode = 123,
        activityName = "com.simprints.id.MyActivity",
        bundleKey = "BUNDLE_KEY",
        request = mockRequest(),
        status = Step.Status.ONGOING
    )

    private fun mockRequest() = FingerprintCaptureRequest(fingerprintsToCapture = emptyList())
}
