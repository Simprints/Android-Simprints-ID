package domain.requests

import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.domain.requests.VerifyRequest
import com.simprints.clientapi.requestFactories.RequestFactory
import com.simprints.core.domain.tokenization.asTokenizable
import com.simprints.moduleapi.app.requests.IAppVerifyRequest
import org.junit.Test

class VerifyRequestTest {

    @Test
    fun `when converting to app request, tokenization flags are set correctly to true`() {
        val result = buildRequest(isTokenized = true).convertToAppRequest() as IAppVerifyRequest
        assertThat(result.isUserIdTokenized).isTrue()
        assertThat(result.isModuleIdTokenized).isTrue()
    }

    @Test
    fun `when converting to app request, tokenization flags are set correctly to false`() {
        val result = buildRequest(isTokenized = false).convertToAppRequest() as IAppVerifyRequest
        assertThat(result.isUserIdTokenized).isFalse()
        assertThat(result.isModuleIdTokenized).isFalse()
    }

    private fun buildRequest(isTokenized: Boolean) = VerifyRequest(
        projectId = RequestFactory.MOCK_PROJECT_ID,
        moduleId = "moduleId".asTokenizable(isTokenized),
        userId = "userId".asTokenizable(isTokenized),
        metadata = RequestFactory.MOCK_METADATA,
        verifyGuid = RequestFactory.MOCK_VERIFY_GUID,
        unknownExtras = emptyMap()
    )
}
