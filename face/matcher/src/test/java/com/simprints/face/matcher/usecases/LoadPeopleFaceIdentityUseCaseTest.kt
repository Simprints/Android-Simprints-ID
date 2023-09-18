package com.simprints.face.matcher.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.face.FaceSample
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.enrolment.records.domain.models.FaceIdentity
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LoadPeopleFaceIdentityUseCaseTest {

    @MockK
    lateinit var enrolmentRecordManager: EnrolmentRecordManager

    private lateinit var useCase: LoadPeopleFaceIdentityUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = LoadPeopleFaceIdentityUseCase(enrolmentRecordManager)
    }

    @Test
    fun `Correctly maps returned results`() = runTest {
        coEvery { enrolmentRecordManager.loadFaceIdentities(any()) } returns flowOf(
            FaceIdentity(
                "personId",
                listOf(FaceSample(byteArrayOf(1, 2, 3), "format", "faceTemplate"))
            )
        )

        val result = useCase.invoke(mockk()).toList().first()

        assertThat(result.faceId).isEqualTo("personId")
        assertThat(result.faces).hasSize(1)
        assertThat(result.faces.first().faceId).isEqualTo("faceTemplate")
        assertThat(result.faces.first().template).isEqualTo(byteArrayOf(1, 2, 3))
    }
}
