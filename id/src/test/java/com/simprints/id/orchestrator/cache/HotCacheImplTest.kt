package com.simprints.id.orchestrator.cache

import android.content.Context.MODE_PRIVATE
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.Application
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.*
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFollowUp.AppConfirmIdentityRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.testtools.TestApplication
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_METADATA
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class HotCacheImplTest {

    private val hotCache by lazy {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val stepEncoder = StepEncoderImpl()
        HotCacheImpl(context.getSharedPreferences("shared", MODE_PRIVATE), stepEncoder)
    }

    @Test
    fun shouldReadEnrolAppRequest() {
        val appRequest = AppEnrolRequest(
            DEFAULT_PROJECT_ID,
            DEFAULT_USER_ID,
            DEFAULT_MODULE_ID,
            DEFAULT_METADATA
        )

        hotCache.appRequest = appRequest
        assertThat(hotCache.appRequest).isEqualTo(appRequest)
    }

    @Test
    fun shouldReadVerifyAppRequest() {
        val appRequest = AppVerifyRequest(
            DEFAULT_PROJECT_ID,
            DEFAULT_USER_ID,
            DEFAULT_MODULE_ID,
            DEFAULT_METADATA,
            GUID1
        )

        hotCache.appRequest = appRequest
        assertThat(hotCache.appRequest).isEqualTo(appRequest)
    }

    @Test
    fun shouldReadIdentifyAppRequest() {
        val appRequest = AppIdentifyRequest(
            DEFAULT_PROJECT_ID,
            DEFAULT_USER_ID,
            DEFAULT_MODULE_ID,
            DEFAULT_METADATA
        )

        hotCache.appRequest = appRequest
        assertThat(hotCache.appRequest).isEqualTo(appRequest)
    }

    @Test
    fun shouldReadConfirmIdentityAppRequest() {
        val appRequest = AppConfirmIdentityRequest(
            DEFAULT_PROJECT_ID,
            DEFAULT_USER_ID,
            DEFAULT_MODULE_ID,
            DEFAULT_METADATA
        )

        hotCache.appRequest = appRequest
        assertThat(hotCache.appRequest).isEqualTo(appRequest)
    }

    @Test
    fun shouldCacheStep() {
        val step = mockStep()

        hotCache.save(listOf(step))
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
            save(listOf(oldStep))
            save(listOf(newStep))
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
            save(listOf(step1, step2))
        }

        val cachedSteps = hotCache.load()
        val cachedStepCount = cachedSteps.size

        assertThat(cachedStepCount).isEqualTo(2)
        assertThat(cachedSteps).contains(step1)
        assertThat(cachedSteps).contains(step2)
    }

    @Test
    fun shouldPreserveStepOrder() {
        val stepCount = 100
        val steps = List(stepCount) { mockStep(it) }

        with(hotCache) {
            save(steps)
        }

        val cachedSteps = hotCache.load()
        val cachedStepCount = cachedSteps.size

        assertThat(cachedStepCount).isEqualTo(stepCount)
        steps.forEachIndexed { index, step ->
            assertThat(step).isEqualTo(cachedSteps[index])
        }
    }

    @Test
    fun shouldClearCache() {
        val step = mockStep()

        with(hotCache) {
            save(listOf(step))
            clearSteps()
        }

        val cachedSteps = hotCache.load()
        val cachedStepCount = cachedSteps.size
        val isStepCached = cachedSteps.contains(step)

        assertThat(cachedStepCount).isEqualTo(0)
        assertThat(isStepCached).isFalse()
    }

    private fun mockStep(requestCode: Int = 123) = Step(
        requestCode = requestCode,
        activityName = "com.simprints.id.MyActivity",
        payloadType = Step.PayloadType.REQUEST,
        bundleKey = "BUNDLE_KEY",
        payload = mockRequest(),
        status = Step.Status.ONGOING
    )

    private fun mockRequest() = FingerprintCaptureRequest(fingerprintsToCapture = emptyList())
}
