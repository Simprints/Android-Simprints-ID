package domain.requests

import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.domain.requests.EnrolLastBiometricsRequest
import com.simprints.clientapi.requestFactories.RequestFactory
import com.simprints.core.domain.tokenization.asTokenized
import com.simprints.moduleapi.app.requests.IAppEnrolLastBiometricsRequest
import org.junit.Test

class EnrollLastBiometricsRequestTest {

    @Test
    fun `when converting to app request, tokenization flags are set correctly`() {
        val result = enrolLastBiometricsRequest.convertToAppRequest() as IAppEnrolLastBiometricsRequest
        assertThat(result.isUserIdTokenized).isTrue()
        assertThat(result.isModuleIdTokenized).isTrue()
    }

    companion object {
        private val USER_ID_TOKENIZED = "userId".asTokenized(isTokenized = true)
        private val MODULE_ID_TOKENIZED = "moduleId".asTokenized(isTokenized = true)
        private val enrolLastBiometricsRequest = EnrolLastBiometricsRequest(
            projectId = RequestFactory.MOCK_PROJECT_ID,
            userId = USER_ID_TOKENIZED,
            moduleId = MODULE_ID_TOKENIZED,
            metadata = RequestFactory.MOCK_METADATA,
            sessionId = RequestFactory.MOCK_SESSION_ID,
            unknownExtras = emptyMap()
        )
    }

}
