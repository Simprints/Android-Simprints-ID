package domain.requests

import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.domain.requests.IdentifyRequest
import com.simprints.clientapi.requestFactories.RequestFactory
import com.simprints.core.domain.tokenization.asTokenized
import com.simprints.moduleapi.app.requests.IAppIdentifyRequest
import org.junit.Test

class IdentifyRequestTest {

    @Test
    fun `when converting to app request, tokenization flags are set correctly to true`() {
        val result = buildRequest(isTokenized = true).convertToAppRequest() as IAppIdentifyRequest
        assertThat(result.isUserIdTokenized).isTrue()
        assertThat(result.isModuleIdTokenized).isTrue()
    }

    @Test
    fun `when converting to app request, tokenization flags are set correctly to false`() {
        val result = buildRequest(isTokenized = false).convertToAppRequest() as IAppIdentifyRequest
        assertThat(result.isUserIdTokenized).isFalse()
        assertThat(result.isModuleIdTokenized).isFalse()
    }

    private fun buildRequest(isTokenized: Boolean) = IdentifyRequest(
        projectId = RequestFactory.MOCK_PROJECT_ID,
        moduleId = "moduleId".asTokenized(isTokenized),
        userId = "userId".asTokenized(isTokenized),
        metadata = RequestFactory.MOCK_METADATA,
        unknownExtras = emptyMap()
    )
}
