package com.simprints.id.orchestrator.cache

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.simprints.id.data.secure.keystore.KeystoreManagerImpl
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintEnrolRequest
import com.simprints.id.orchestrator.cache.crypto.step.StepEncoderImpl
import com.simprints.id.orchestrator.steps.Step
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class HotCacheImplAndroidTest {

    private val hotCache by lazy {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferences = context.getSharedPreferences("file_name", Context.MODE_PRIVATE)
        val keystoreManager = KeystoreManagerImpl(context)
        val stepEncoder = StepEncoderImpl(keystoreManager)
        HotCacheImpl(preferences, stepEncoder)
    }

    @Test
    fun shouldCacheStep() {
        val step = mockStep()

        hotCache.save(step)
        val cachedSteps = hotCache.load()

        val isStepCached = cachedSteps.contains(step)
        val cachedStepCount = cachedSteps.size

        assertThat(cachedStepCount, `is`(1))
        assertThat(isStepCached, `is`(true))
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

        assertThat(cachedStepCount, `is`(1))
        assertThat(cachedSteps.first(), `is`(newStep))
        assertThat(isOldStepCached, `is`(false))
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

        assertThat(cachedStepCount, `is`(2))
        assertThat(cachedSteps.contains(step1), `is`(true))
        assertThat(cachedSteps.contains(step2), `is`(true))
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

        assertThat(cachedStepCount, `is`(0))
        assertThat(isStepCached, `is`(false))
    }

    private fun mockStep() = Step(
        requestCode = 123,
        activityName = "com.simprints.id.MyActivity",
        bundleKey = "BUNDLE_KEY",
        request = mockRequest(),
        status = Step.Status.ONGOING
    )

    private fun mockRequest() = FingerprintEnrolRequest(
        "projectId",
        "userId",
        "moduleId",
        "metadata",
        "language",
        mapOf(),
        true,
        "programmeName",
        "organisationName"
    )

}
