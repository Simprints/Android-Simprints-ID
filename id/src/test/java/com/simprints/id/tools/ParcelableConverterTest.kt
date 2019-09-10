package com.simprints.id.tools

import android.os.Parcelable
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintEnrolRequest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.notNullValue
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
class ParcelableConverterTest {

    private lateinit var converter: ParcelableConverter

    @Test
    fun withParcelableInput_shouldConvertToByteArray() {
        converter = ParcelableConverter(getParcelable())
        val bytes = converter.toBytes()
        converter.recycle()

        assertThat(bytes, notNullValue())
        assertThat(bytes.size, greaterThan(0))
    }

    @Test
    fun withByteArrayInput_shouldConvertToParcel() {
        val bytes = prepareByteArray()
        converter = ParcelableConverter(bytes)
        val parcel = converter.getParcel()
        converter.recycle()

        assertThat(parcel, notNullValue())
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    private fun getParcelable(): Parcelable {
        return FingerprintEnrolRequest(
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

    private fun prepareByteArray(): ByteArray {
        converter = ParcelableConverter(getParcelable())
        val bytes = converter.toBytes()
        converter.recycle()
        return bytes
    }

}
