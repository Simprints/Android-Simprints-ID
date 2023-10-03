package domain.requests

import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.domain.requests.EnrolLastBiometricsRequest
import com.simprints.clientapi.requestFactories.RequestFactory
import com.simprints.core.domain.tokenization.asTokenized
import com.simprints.moduleapi.app.requests.IAppEnrolLastBiometricsRequest
import org.junit.Test

class EnrollLastBiometricsRequestTest {

    @Test
    fun `when converting to app request, tokenization flags are set correctly to true`() {
        val result =
            buildRequest(isTokenized = true).convertToAppRequest() as IAppEnrolLastBiometricsRequest
        assertThat(result.isUserIdTokenized).isTrue()
        assertThat(result.isModuleIdTokenized).isTrue()
    }

    @Test
    fun `when converting to app request, tokenization flags are set correctly to false`() {
        val result =
            buildRequest(isTokenized = false).convertToAppRequest() as IAppEnrolLastBiometricsRequest
        assertThat(result.isUserIdTokenized).isFalse()
        assertThat(result.isModuleIdTokenized).isFalse()
    }

    private fun buildRequest(isTokenized: Boolean) = EnrolLastBiometricsRequest(
        projectId = RequestFactory.MOCK_PROJECT_ID,
        moduleId = "moduleId".asTokenized(isTokenized),
        userId = "userId".asTokenized(isTokenized),
        metadata = RequestFactory.MOCK_METADATA,
        sessionId = RequestFactory.MOCK_SESSION_ID,
        unknownExtras = emptyMap()
    )
}
