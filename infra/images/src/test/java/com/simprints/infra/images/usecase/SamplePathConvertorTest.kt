package com.simprints.infra.images.usecase

import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.Modality
import com.simprints.infra.images.model.Path
import org.junit.Before
import org.junit.Test

class SamplePathConvertorTest {
    private lateinit var pathUtil: SamplePathConverter

    @Before
    fun setUp() {
        pathUtil = SamplePathConverter()
    }

    @Test
    fun `should create a valid path for face sample`() {
        val expectedPath = "sessions/sessionId/faces/captureEventId.jpg"
        val result = pathUtil.create(
            sessionId = "sessionId",
            modality = Modality.FACE,
            sampleId = "captureEventId",
            fileExtension = "jpg",
        )

        assertThat(result.compose()).isEqualTo(expectedPath)
    }

    @Test
    fun `should create a valid path for fingerprint sample`() {
        val expectedPath = "sessions/sessionId/fingerprints/captureEventId.swq"

        val result = pathUtil.create(
            sessionId = "sessionId",
            modality = Modality.FINGERPRINT,
            sampleId = "captureEventId",
            fileExtension = "swq",
        )

        assertThat(result.compose()).isEqualTo(expectedPath)
    }

    @Test
    fun `extracts fingerprint sample data from path`() {
        val result = pathUtil.extract(Path("sessions/sessionId/fingerprints/captureEventId.swq"))

        assertThat(result).isNotNull()
        assertThat(result?.sessionId).isEqualTo("sessionId")
        assertThat(result?.sampleId).isEqualTo("captureEventId")
        assertThat(result?.modality).isEqualTo(Modality.FINGERPRINT)
    }

    @Test
    fun `extracts face sample data from path`() {
        val result = pathUtil.extract(Path("sessions/sessionId/faces/captureEventId.swq"))

        assertThat(result).isNotNull()
        assertThat(result?.sessionId).isEqualTo("sessionId")
        assertThat(result?.sampleId).isEqualTo("captureEventId")
        assertThat(result?.modality).isEqualTo(Modality.FACE)
    }

    @Test
    fun `extracts sample data from path with project segments`() {
        val result = pathUtil.extract(Path("projects/projectId/sessions/sessionId/fingerprints/captureEventId.swq"))

        assertThat(result).isNotNull()
        assertThat(result?.sessionId).isEqualTo("sessionId")
        assertThat(result?.sampleId).isEqualTo("captureEventId")
        assertThat(result?.modality).isEqualTo(Modality.FINGERPRINT)
    }

    @Test
    fun `returns null if path is too short`() {
        val result = pathUtil.extract(Path("captureEventId.swq"))

        assertThat(result).isNull()
    }

    @Test
    fun `returns null if path does not contain sessions segment`() {
        val result = pathUtil.extract(Path("sessionId/fingerprints/captureEventId.swq"))

        assertThat(result).isNull()
    }
}
