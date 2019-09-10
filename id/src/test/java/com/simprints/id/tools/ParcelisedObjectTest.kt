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
class ParcelisedObjectTest {

    private lateinit var parcelisedObject: ParcelisedObject

    @Test
    fun withParcelableInput_shouldConvertToByteArray() {
        parcelisedObject = ParcelisedObject(getParcelable())
        val bytes = parcelisedObject.toBytes()
        parcelisedObject.recycle()

        assertThat(bytes, notNullValue())
        assertThat(bytes.size, greaterThan(0))
    }

    @Test
    fun withByteArrayInput_shouldConvertToParcel() {
        val bytes = prepareByteArray()
        parcelisedObject = ParcelisedObject(bytes)
        val parcel = parcelisedObject.getParcel()
        parcelisedObject.recycle()

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
        parcelisedObject = ParcelisedObject(getParcelable())
        val bytes = parcelisedObject.toBytes()
        parcelisedObject.recycle()
        return bytes
    }

}
