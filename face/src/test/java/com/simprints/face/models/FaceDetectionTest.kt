package com.simprints.face.models


import com.google.common.truth.Truth
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class FaceDetectionTest {
    @Test
    fun `convert FaceDetection toFaceSample succeed when face is empty`() {
        //Given
        val faceDetection = FaceDetection(
            mockk(), null, mockk(), mockk()
        )
        //When
        val sample = faceDetection.toFaceSample()
        //Then

        Truth.assertThat(sample.faceId).isEqualTo(faceDetection.id)
        Truth.assertThat(sample.format).isEmpty()
        Truth.assertThat(sample.imageRef).isEqualTo(faceDetection.securedImageRef)
    }
    @Test
    fun `convert FaceDetection toFaceSample succeed when face has values`() {
        //Given
        val faceDetection = FaceDetection(
            mockk(), mockk{
                          every { format } returns "format"
            }, mockk(), mockk()
        )
        //When
        val sample = faceDetection.toFaceSample()
        //Then

        Truth.assertThat(sample.faceId).isEqualTo(faceDetection.id)
        Truth.assertThat(sample.format).isEqualTo(faceDetection.face?.format)
        Truth.assertThat(sample.imageRef).isEqualTo(faceDetection.securedImageRef)
    }
}
