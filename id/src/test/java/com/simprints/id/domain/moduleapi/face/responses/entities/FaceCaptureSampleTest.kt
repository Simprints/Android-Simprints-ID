package com.simprints.id.domain.moduleapi.face.responses.entities

import com.google.common.truth.Truth
import io.mockk.mockk
import org.junit.Test

class FaceCaptureSampleTest {
    @Test
    fun `test convert FromDomainToModuleApi and then fromModuleApiToDomain will succeed`() {
        //Given
        val captureSample = FaceCaptureSample(
            faceId = "id",
            format = "format",
            template = byteArrayOf(0),
            imageRef = mockk()
        )
        //When
        val convertedSample = captureSample.fromDomainToModuleApi().fromModuleApiToDomain()
        //Then
        Truth.assertThat(convertedSample.faceId).isEqualTo(captureSample.faceId)
        Truth.assertThat(convertedSample.format).isEqualTo(captureSample.format)

    }

}
