package com.simprints.fingerprint.data.domain.fingerprint

import android.os.Parcel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.simprints.fingerprint.testtools.FingerprintGenerator
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FingerprintTest {

    @Test
    fun `test parcelize fingerprint object returns the same data`() {
        //Given
        val fingerprint = FingerprintGenerator.generateRandomFingerprint()
        val parcel = Parcel.obtain()
        fingerprint.writeToParcel(parcel, 0)
        val dataPosition = parcel.dataPosition()
        parcel.setDataPosition(0)

        //When
        val fingerprintFromParcel = Fingerprint.CREATOR.createFromParcel(parcel)


        //Then
        Truth.assertThat(fingerprint.fingerId).isEqualTo(fingerprintFromParcel.fingerId)
        Truth.assertThat(fingerprint.format).isEqualTo(fingerprintFromParcel.format)
        Truth.assertThat(fingerprint.templateBytes.size)
            .isEqualTo(fingerprintFromParcel.templateBytes.size)
        Truth.assertThat(dataPosition).isEqualTo(parcel.dataPosition())
    }
}
