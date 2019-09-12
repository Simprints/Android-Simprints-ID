package com.simprints.id.tools

import android.os.Parcelable
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintEnrolRequest
import com.simprints.id.orchestrator.steps.Step
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
class ParcelableConverterTest {

    private lateinit var converter: ParcelableConverter

    @Test
    fun withParcelableInput_shouldConvertToByteArray() {
        val bytes = prepareByteArray()

        assertThat(bytes, notNullValue())
        assertThat(bytes.size, greaterThan(0))
    }

    @Test
    fun withByteArrayInput_shouldConvertToParcel() {
        val bytes = prepareByteArray()
        converter = ParcelableConverter(bytes)
        val parcel = converter.getParcel()
        val step = Step.createFromParcel(parcel)
        converter.recycle()

        assertThat(parcel, notNullValue())
        assertThat(step.requestCode, `is`(REQUEST_CODE))
        assertThat(step.activityName, `is`(ACTIVITY_NAME))
        assertThat(step.bundleKey, `is`(BUNDLE_KEY))
        assertThat(step.request, `is`(request))
        assertThat(step.status, `is`(Step.Status.ONGOING))
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    private fun getParcelable(): Parcelable {
        return Step(
            REQUEST_CODE,
            ACTIVITY_NAME,
            BUNDLE_KEY,
            request,
            Step.Status.ONGOING
        )
    }

    private fun prepareByteArray(): ByteArray {
        converter = ParcelableConverter(getParcelable())
        val bytes = converter.toBytes()
        converter.recycle()
        return bytes
    }

    companion object {
        private const val REQUEST_CODE = 123
        private const val ACTIVITY_NAME = "com.simprints.id.MyActivity"
        private const val BUNDLE_KEY = "BUNDLE_KEY"

        private val request: Step.Request = FingerprintEnrolRequest(
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
