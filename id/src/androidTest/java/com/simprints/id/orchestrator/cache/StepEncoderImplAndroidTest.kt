package com.simprints.id.orchestrator.cache

import androidx.test.platform.app.InstrumentationRegistry
import com.simprints.id.data.secure.keystore.KeystoreManagerImpl
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintEnrolRequest
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintEnrolResponse
import com.simprints.id.orchestrator.steps.Step
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test
import org.koin.core.context.stopKoin
import java.util.*

class StepEncoderImplAndroidTest {

    private val stepEncoder by lazy {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val keystoreManager = KeystoreManagerImpl(context)
        StepEncoderImpl(keystoreManager)
    }

    private val step = buildStep()

    @Test
    fun shouldEncodeStepToString() {
        val encodedString = stepEncoder.encode(step)

        assertThat(encodedString, notNullValue())
    }

    @Test
    fun shouldDecodeStringToStep() {
        val encodedString = stepEncoder.encode(step)
        val decodedStep = stepEncoder.decode(encodedString)

        assertThat(decodedStep?.requestCode, `is`(REQUEST_CODE))
        assertThat(decodedStep?.activityName, `is`(ACTIVITY_NAME))
        assertThat(decodedStep?.bundleKey, `is`(BUNDLE_KEY))
        assertThat(decodedStep?.request, `is`(request))
        assertThat(decodedStep?.status, `is`(Step.Status.ONGOING))
        assertThat(decodedStep?.result, `is`(result))
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    private fun buildStep(): Step {
        return Step(
            REQUEST_CODE,
            ACTIVITY_NAME,
            BUNDLE_KEY,
            request,
            Step.Status.ONGOING
        ).also {
            it.result = result
        }
    }

    companion object {
        private const val REQUEST_CODE = 123
        private const val ACTIVITY_NAME = "com.simprints.id.MyActivity"
        private const val BUNDLE_KEY = "BUNDLE_KEY"

        val request: Step.Request = mockRequest()
        val result: Step.Result = FingerprintEnrolResponse(UUID.randomUUID().toString())

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

}
